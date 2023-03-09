package cn.weicm;

public class Main {
    public static void main(String[] args) {
        System.out.println(UJson.isValid(""));
        System.out.println(UJson.isValid("{"));
        System.out.println(UJson.isValid("1"));
        System.out.println(UJson.isValid("-"));
        System.out.println(UJson.isValid("a"));
        System.out.println(UJson.isValid("{}"));
        System.out.println(UJson.isValid("[]"));
    }
}
