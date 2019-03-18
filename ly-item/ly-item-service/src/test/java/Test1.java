public class Test1 {

    public Test1() {
        System.out.println("hello test1");//4
    }

    {
        System.out.println("非静态 = test1");//3
    }

    static {
        System.out.println("静态 + test 1");//1
    }

    public static void main(String[] args) {
        //new Test1();
        System.out.println("------1--------");
        new Test2();
        System.out.println("------2--------");
    }


}

class Test2 extends Test1 {

    public Test2() {
        super();
        System.out.println("hello test2");//6
    }

    {
        System.out.println("非静态 = test2");// 5
    }

    static {
        System.out.println("静态 = test 2");//2
    }

}
