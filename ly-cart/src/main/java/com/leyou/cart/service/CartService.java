package com.leyou.cart.service;

import com.leyou.auth.pojo.UserInfo;
import com.leyou.cart.interceptor.LoginInterceptor;
import com.leyou.cart.pojo.Cart;
import com.leyou.common.pojo.Message;
import com.leyou.common.utils.JsonUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
public class CartService {

    @Autowired
    private StringRedisTemplate redisTemplate;

    private final static String KEY_PREFIX = "ly:cart:uid:";

    private final static String KEY_DELETE = "ly:cart:delete:";

    private static final Logger LOGGER = LoggerFactory.getLogger(CartService.class);

    /**
     * 添加购物车
     *
     * @param cart 购物车对象
     */
    public void addCart(Cart cart) {
        // 获取登录用户
        UserInfo userInfo = LoginInterceptor.getLoginUser();
        // redis的key
        String key = KEY_PREFIX + userInfo.getId();
        // 获取hash操作对象
        BoundHashOperations<String, Object, Object> hashOps = this.redisTemplate.boundHashOps(key);
        // 查询是否存在
        Long skuId = cart.getSkuId();
        Integer num = cart.getNum();
        // 判断缓存中是否已经存在该键
        Boolean bool = hashOps.hasKey(skuId.toString());
        if (bool) {
            // 存在，获取购物车数据
            String json = hashOps.get(skuId.toString()).toString();
            cart = JsonUtils.parse(json, Cart.class);
            // 修改购物车数量
            cart.setNum(num + cart.getNum());
        } else {
            // 不存在，设置购物车所属用户的id
            cart.setUserId(userInfo.getId());
        }
        // 将购物车对象，写入缓存中
        hashOps.put(cart.getSkuId().toString(), JsonUtils.serialize(cart));
    }

    /**
     * 查询购物车
     *
     * @return 购物车集合
     */
    public List<Cart> queryCartList() {
        // 获取登录用户
        UserInfo userInfo = LoginInterceptor.getLoginUser();
        // 判断是否存在购物车
        String key = KEY_PREFIX + userInfo.getId();
        if (!this.redisTemplate.hasKey(key)) return null;
        // 获取缓存中的购物车
        BoundHashOperations<String, Object, Object> hashOps = this.redisTemplate.boundHashOps(key);
        List<Object> cartList = hashOps.values();
        // 判断该购物车是否为null
        if (CollectionUtils.isEmpty(cartList)) return null;
        // 查询购物车集合中的购物车商品，并返回
        return cartList.stream().map(cart -> JsonUtils.parse(cart.toString(), Cart.class)).collect(Collectors.toList());
    }

    public void updateNum(Long skuId, Integer num) {
        // 获取登录
        UserInfo userInfo = LoginInterceptor.getLoginUser();
        String key = KEY_PREFIX + userInfo.getId();
        BoundHashOperations<String, Object, Object> hashOps = this.redisTemplate.boundHashOps(key);
        // 获取购物车
        String json = hashOps.get(skuId.toString()).toString();
        Cart cart = JsonUtils.parse(json, Cart.class);
        // 修改数量
        cart.setNum(num);
        // 写入购物车
        hashOps.put(skuId.toString(), JsonUtils.serialize(cart));
    }

    public Cart deleteCart(String skuId) {
        // 获取登录用户名
        UserInfo userInfo = LoginInterceptor.getLoginUser();
        String key = KEY_PREFIX + userInfo.getId();
        BoundHashOperations<String, Object, Object> hashOps = this.redisTemplate.boundHashOps(key);
        Cart cart = JsonUtils.parse(hashOps.get(skuId).toString(), Cart.class);
        // 删除该商品
        hashOps.delete(skuId);
        // 删除的商品添加到缓存中，时限为30分钟
        this.addDeletedCart(cart);
        return cart;
    }

    private void addDeletedCart(Cart cart) {
        if (cart == null) return;
        // 获取缓存的键
        String key = KEY_DELETE + LoginInterceptor.getLoginUser().getId();
        // 获取hash操作对象
        BoundHashOperations<String, Object, Object> hashOps = this.redisTemplate.boundHashOps(key);
        this.redisTemplate.expire(key, 30, TimeUnit.MINUTES);
        // 查询缓存对象中是否有该键
        Boolean bool = hashOps.hasKey(cart.getSkuId().toString());
        if (bool) {
            // 如果存在，修改商品数量
            String json = hashOps.get(cart.getSkuId().toString()).toString();
            Cart c = JsonUtils.parse(json, Cart.class);
            cart.setNum(c.getNum() + cart.getNum());
        }
        // 将cart存入缓存中
        hashOps.put(cart.getSkuId().toString(), JsonUtils.serialize(cart));
    }

    /**
     * 合并购物车
     *
     * @param cartList 购物车的集合
     */
    public void mergeCart(List<Cart> cartList) {
        // 获取登录用户名
        UserInfo userInfo = LoginInterceptor.getLoginUser();
        // 获取缓存中的购物车数据
        String key = KEY_PREFIX + userInfo.getId();
        BoundHashOperations<String, Object, Object> hashOps = this.redisTemplate.boundHashOps(key);
        cartList.forEach(cart -> {
            if (hashOps.hasKey(cart.getSkuId().toString())) {
                // 如果缓存中存在该键，则修改该购物车商品的数量即可
                String json = hashOps.get(cart.getSkuId().toString()).toString();
                Cart cartRedis = JsonUtils.parse(json, Cart.class);
                cartRedis.setNum(cart.getNum() + cartRedis.getNum());
                // 重新存入缓存中
                hashOps.put(cartRedis.getSkuId().toString(), JsonUtils.serialize(cartRedis));
            } else {
                // 如果不存在则设置该购物车商品所属用户，新增购物车
                cart.setUserId(userInfo.getId());
                // 新增购物车
                hashOps.put(cart.getSkuId().toString(), JsonUtils.serialize(cart));
            }
        });
    }

    /**
     * 删除选中了的商品购物车，
     * 并将选中删除的商品购物车返回到前台页面
     *
     * @param skuIds 商品id集合
     */
    public List<Cart> deleteCheckedCart(List<Long> skuIds) {
        // 获取登录用户名
        UserInfo userInfo = LoginInterceptor.getLoginUser();
        String key = KEY_PREFIX + userInfo.getId();
        BoundHashOperations<String, Object, Object> hashOps = this.redisTemplate.boundHashOps(key);
        List<Cart> cartList = hashOps.values().stream().map(obj -> JsonUtils.parse(obj.toString(), Cart.class))
                .filter(cart -> skuIds.contains(cart.getSkuId())).collect(Collectors.toList());
        // 删除选中的商品
        skuIds.forEach(skuId -> hashOps.delete(skuId.toString()));
        // 删除的商品添加到缓存中，时限为30分钟
        cartList.forEach(this::addDeletedCart);
        return cartList;
    }

    /**
     * 从购物车中移除下了单的商品
     *
     * @param message 消息
     */
    public void removeSkus(Message message) {
        System.out.println(message);
        String key = KEY_PREFIX + message.getId();
        BoundHashOperations<String, Object, Object> hashOps = this.redisTemplate.boundHashOps(key);
        message.getIds().forEach(id -> hashOps.delete(id.toString()));
    }

    /**
     * 查询已删除的商品
     *
     * @return 过期商品集合
     */
    public List<Cart> queryPastedCart() {
        // 获取存入缓存的key
        String key = KEY_DELETE + LoginInterceptor.getLoginUser().getId();
        // 如果缓存中没有该key，返回null
        if (!this.redisTemplate.hasKey(key)) return null;
        // 存在则查询所有的购物车
        List<Object> list = this.redisTemplate.boundHashOps(key).values();
        // 判断该集合是否为null
        if (CollectionUtils.isEmpty(list)) return null;
        // 转换类型，返回该集合
        return list.stream().map(obj -> JsonUtils.parse(obj.toString(), Cart.class)).collect(Collectors.toList());
    }

    public void reAddToCart(Cart cart) {
        // 先将商品添加到购物车缓存中
        this.addCart(cart);
        // 再从删除的购物车缓存中移除该商品
        String key = KEY_DELETE + LoginInterceptor.getLoginUser().getId();
        BoundHashOperations<String, Object, Object> hashOps = this.redisTemplate.boundHashOps(key);
        hashOps.delete(cart.getSkuId().toString());
    }
}
