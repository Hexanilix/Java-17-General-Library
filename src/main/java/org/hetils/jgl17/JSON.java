package org.hetils.jgl17;


import org.hetils.jgl17.oodp.OODP;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class JSON {
    public static abstract class Abstr {
        UUID id = UUID.randomUUID();
    }

    public static class A extends Abstr {
        String t = "tja";

        public A() {}
        public A(String t) {this.t = t;}

        @Override
        public String toString() {
            return "A{" +
                    "t='" + t + '\'' +
                    "id=" + id +
                    '}';
        }
    }

    public static class B extends Abstr {
        public int num;
        List<A> l = new ArrayList<>();

        public B() { num = 0; }
        public B(int n) { num = n; }

        @Override
        public String toString() {
            return "B{" +
                    "id=" + id +
                    ", num=" + num +
                    ", l=" + l +
                    '}';
        }
    }

    public static class Test {
        public A a = new A("ilhg");
        public B b = new B(2);

        public Test() {  }
    }

    public static void main(String[] args) {
        OODP dp = new OODP();
        B b = new B();
        b.l.add(new A());
        String s = dp.prettyUp(dp.toOodp(b));
        System.out.println(s);
        System.out.println(dp.map(s));
        B b2 = dp.map(dp.map(s).toString()).as(B.class);
        System.out.println(b2.toString());
        b2.l.add(new A());
        System.out.println(b2);

//        String s = dp.toOodp(new int[]{1, 4, 5});
//        System.out.println(s);
//        System.out.println(Arrays.toString(dp.map(s).asIntArray()));
    }
}
