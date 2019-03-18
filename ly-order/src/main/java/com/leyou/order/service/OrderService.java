package com.leyou.order.service;

import com.leyou.auth.pojo.UserInfo;
import com.leyou.common.exception.LyException;
import com.leyou.common.pojo.Message;
import com.leyou.common.utils.IdWorker;
import com.leyou.common.utils.JsonUtils;
import com.leyou.order.enums.PayState;
import com.leyou.order.enums.PayStatusEnum;
import com.leyou.order.interceptor.LoginInterceptor;
import com.leyou.order.mapper.*;
import com.leyou.order.pojo.*;
import com.leyou.order.enums.OrderStatusEnum;
import com.leyou.order.utils.PayHelper;
import com.leyou.seckill.mapper.SeckillGoodsMapper;
import com.leyou.seckill.pojo.Item;
import com.leyou.seckill.pojo.SeckillGoods;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class OrderService {

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private StockMapper stockMapper;

    private static final String KEY_PREFIX = "ly:cart:uid:";

    private final static Logger LOGGER = LoggerFactory.getLogger(OrderService.class);

    /**
     * 查询选中欲下单的商品sku
     *
     * @param skuIds 商品skuId
     * @return 返回选中的商品集合
     */
    public List<Cart> queryCarts(List<Long> skuIds) {
        // 获取用户登录
        UserInfo userInfo = LoginInterceptor.getLoginUser();
        // 判断是否存在该用户的购物车
        String key = KEY_PREFIX + userInfo.getId();
        if (!redisTemplate.hasKey(key)) return null;
        // 获取缓存中的购物车
        BoundHashOperations<String, Object, Object> hashOps = this.redisTemplate.boundHashOps(key);
        List<Object> cartList = hashOps.values();
        // 判断购物车是否为null
        if (CollectionUtils.isEmpty(cartList)) return null;
        // 过滤掉购物车中的商品
        List<Cart> carts = cartList.stream().map(cart -> JsonUtils.parse(cart.toString(), Cart.class))
                .filter(cart -> skuIds.contains(cart.getSkuId())).collect(Collectors.toList());
        // 查询商品的库存量
        carts.forEach(cart -> cart.setStock(this.stockMapper.selectStockBySkuId(cart.getSkuId())));
        return carts;
    }

    @Autowired
    private IdWorker idWorker;

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private OrderDetailMapper orderDetailMapper;

    @Autowired
    private OrderStatusMapper orderStatusMapper;

    /**
     * 生成订单相关数据
     */
    @Transactional
    public Order createOrder(OrderDTO orderDTO) {
        // 获取用户信息
        UserInfo userInfo = LoginInterceptor.getLoginUser();
        // 生成订单的id
        long orderId = idWorker.nextId();
        Order order = this.makeOrder(orderDTO, userInfo, orderId);
        // 减少库存
        orderDTO.getOrderDetails().forEach(cart -> {
            int count = this.stockMapper.decreaseStock(cart.getNum(), cart.getSkuId());
            if (count != 1) {
                throw new RuntimeException("库存量不足");
            }
        });
        // 5. 向MQ发送消息，递交下了单的商品信息message
        List<Long> ids = orderDTO.getOrderDetails().stream().map(cart -> cart.getSkuId()).collect(Collectors.toList());
        Message message = new Message(userInfo.getId(), ids);
        this.sendMessage(message, "remove");
        // 打印日志
        LOGGER.info("生成订单，订单编号：{}，用户id：{}", orderId, userInfo.getId());
        return order;
    }

    private Order makeOrder(OrderDTO orderDTO, UserInfo userInfo, long orderId) {
        // 1.组织order订单数据
        Order order = this.orgOrder(orderDTO, orderId, userInfo);
        // 保存到数据库中
        this.orderMapper.insertSelective(order);
        // 2.组织订单详情oderDetail数据
        List<OrderDetail> orderDetailList = orderDTO.getOrderDetails();
        orderDetailList.forEach(orderDetail -> orderDetail.setOrderId(orderId));
        // 保存到数据库中
        orderDetailList.forEach(o -> this.orderDetailMapper.insert(o));
        // 3.组织订单状态数据信息
        OrderStatus orderStatus = this.orgOrderStatus(orderId);
        // 保存到数据库中
        this.orderStatusMapper.insert(orderStatus);
        return order;
    }

    @Transactional
    public Order createSeckillOrder(OrderDTO orderDTO) {
        // 获取用户信息
        UserInfo userInfo = LoginInterceptor.getLoginUser();
        // 生成订单的id
        long orderId = idWorker.nextId();
        // 生成订单，订单详情，订单状态
        Order order = this.makeOrder(orderDTO, userInfo, orderId);
        // 从缓存中更新秒杀商品数量
        String skuId = orderDTO.getOrderDetails().get(0).getSkuId().toString();
        BoundHashOperations<String, Object, Object> hashOps = this.redisTemplate.boundHashOps(SECKILL);
        SeckillGoods seckillGoods = JsonUtils.parse(hashOps.get(skuId).toString(), SeckillGoods.class);
        seckillGoods.setSeckillTotal(seckillGoods.getSeckillTotal() + 1);
        hashOps.put(skuId, JsonUtils.serialize(seckillGoods));
        return order;
    }

    @Autowired
    private AmqpTemplate amqpTemplate;

    // 发送消息到rabbieMQ的方法
    private void sendMessage(Message message, String type) {
        List<Long> ids = message.getIds();
        if (CollectionUtils.isEmpty(ids) || message.getId() == null) return;
        this.amqpTemplate.convertAndSend("order." + type, message);
    }

    @Autowired
    private PayHelper payHelper;

    @Autowired
    private PayLogService payLogService;

    public String generateUrl(Long orderId) {
        // 根据orderId查找Order类对象
        Order order = this.queryOrderByOderId(orderId);
        // 判断订单状态，如果已支付，不再创建支付请求
        if (order.getOrderStatus().getStatus() != OrderStatusEnum.INIT.value()) {
            // 已支付或关闭，不再生成支付链接
            throw new LyException(HttpStatus.BAD_REQUEST, "订单状态不正确！");
        }
        // 生成付款链接
        String url = payHelper.createPayUrl(orderId, "测试", 1L);
        // 生成支付日志
        this.payLogService.createPayLog(orderId, order.getActualPay());
        if (StringUtils.isBlank(url)) {
            throw new LyException(HttpStatus.INTERNAL_SERVER_ERROR, "付款路径生成异常");
        }
        return url;
    }

    // 根据订单编号orderId查找类Order
    private Order queryOrderByOderId(Long orderId) {
        // 查号Order表
        Order order = new Order();
        order.setOrderId(orderId);
        order = this.orderMapper.selectOne(order);
        // 查找OrderStatus数据
        OrderStatus orderStatus = this.orderStatusMapper.selectByPrimaryKey(orderId);
        order.setOrderStatus(orderStatus);
        // 查找订单详情集合
        OrderDetail detail = new OrderDetail();
        detail.setOrderId(orderId);
        List<OrderDetail> details = this.orderDetailMapper.select(detail);
        order.setOrderDetailList(details);
        return order;
    }

    // 生成订单状态信息方法
    private OrderStatus orgOrderStatus(long orderId) {
        OrderStatus orderStatus = new OrderStatus();
        orderStatus.setOrderId(orderId);
        orderStatus.setStatus(OrderStatusEnum.INIT.value());
        orderStatus.setCreateTime(new Date());
        return orderStatus;
    }

    // 生成订单信息
    private Order orgOrder(OrderDTO orderDTO, long orderId, UserInfo userInfo) {
        Order order = new Order();
        order.setOrderId(orderId);
        order.setTotalPay(orderDTO.getTotalPrice());
        order.setActualPay(orderDTO.getActualPrice());
        order.setPaymentType(orderDTO.getPaymentType());
        order.setPromotionIds("");
        order.setCreateTime(new Date());
        order.setUserId(orderDTO.getAddress().getUserId());
        order.setBuyerNick(userInfo.getUsername());
        order.setBuyerRate(false);
        order.setBuyerMessage("");
        Address address = orderDTO.getAddress();
        order.setReceiver(address.getName());
        order.setReceiverMobile(address.getPhone());
        order.setReceiverState(address.getState());
        order.setReceiverCity(address.getCity());
        order.setReceiverDistrict(address.getDistrict());
        order.setReceiverAddress(address.getAddress());
        order.setReceiverZip(address.getZipCode());
        return order;
    }

    @Transactional
    public void handleNotify(Map<String, String> msg) {
        payHelper.handleNotify(msg);
        System.out.println("msg = " + msg);
    }

    @Autowired
    private PayLogMapper payLogMapper;

    public Integer queryStatus(Long orderId) {
        // 优先去支付日志表中查询状态
        PayLog payLog = this.payLogMapper.selectByPrimaryKey(orderId);
        // 如果是未支付，则去调用微信查询支付状态接口
        if (payLog == null || PayStatusEnum.NOT_PAY.value() == payLog.getStatus()) {
            return payHelper.queryPayState(orderId).getValue();
        }
        // 如果已经成功支付，返回1.
        if (PayStatusEnum.SUCCESS.value() == payLog.getStatus()) {
            return PayState.SUCCESS.getValue();
        }
        // 如果是其它状态，则认为支付失败，返回2
        return PayState.FAIL.getValue();
    }

    /**
     * 该功能应是物流微服务完成的
     *
     * @param orderId 订单id
     */
    @Transactional
    public void updateOrderToShipment(Long orderId) {
        // 根据订单id查询该订单
        Order order = this.orderMapper.selectByPrimaryKey(orderId);
        String[] arr = {"顺丰快递", "中通快递", "申通快递", "京东快递", "圆通快递", "韵达快递"};
        int num = new Random().nextInt(6);
        order.setShippingName(arr[num]);
        order.setShippingCode(idWorker.nextId() + "");
        OrderStatus orderStatus = this.orderStatusMapper.selectByPrimaryKey(orderId);
        orderStatus.setStatus(OrderStatusEnum.DELIVERED.value());
        orderStatus.setConsignTime(new Date());
        this.orderStatusMapper.updateByPrimaryKeySelective(orderStatus);
        this.orderMapper.updateByPrimaryKeySelective(order);
    }

    private static final String SECKILL = "ly:seckill:goods";

    public List<Cart> querySeckillGoods(Long skuId) {
        List<Cart> carts = new ArrayList<>();
        UserInfo userInfo = LoginInterceptor.getLoginUser();
        if (userInfo == null || !this.redisTemplate.hasKey(SECKILL)) return null;
        BoundHashOperations<String, Object, Object> hashOps = this.redisTemplate.boundHashOps(SECKILL);
        if (hashOps.get(skuId.toString()) == null) return null;
        SeckillGoods seckillGoods = JsonUtils.parse(hashOps.get(skuId.toString()).toString(), SeckillGoods.class);
        carts.add(this.createCart(seckillGoods));
        return carts;
    }

    private static final String DETAIL = "ly:seckill:detail";

    private Cart createCart(SeckillGoods seckillGoods) {
        Cart cart = new Cart();
        cart.setImage(seckillGoods.getImages());
        cart.setStock(seckillGoods.getSeckillStock() - seckillGoods.getSeckillTotal());
        cart.setOwnSpec(seckillGoods.getOwnSpec());
        cart.setPrice(seckillGoods.getPrice());
        cart.setSecPrice(seckillGoods.getSecPrice());
        cart.setSkuId(seckillGoods.getSkuId());
        cart.setTitle(seckillGoods.getTitle());
        cart.setNum(1);
        Item item = JsonUtils.parse(this.redisTemplate.boundHashOps(DETAIL).get(cart.getSkuId().toString()).toString(), Item.class);
        cart.setParamMap(item.getData().get("paramMap"));
        return cart;
    }
}
