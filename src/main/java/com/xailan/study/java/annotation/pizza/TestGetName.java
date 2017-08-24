package com.xailan.study.java.annotation.pizza;

public class TestGetName {
	class Name {
		class Inner {
		}
	}

	public static void main(String[] args) {
		System.out.println("Name.class.getCanonicalName(): " + Name.class.getCanonicalName());
		System.out.println("Name.class.getName():          " + Name.class.getName());
		System.out.println("Name.class.getSimpleName():    " + Name.class.getSimpleName());

		System.out.println("Name.Inner.class.getCanonicalName(): " + Name.Inner.class.getCanonicalName());
		System.out.println("Name.Inner.class.getName():          " + Name.Inner.class.getName());
		System.out.println("Name.Inner.class.getSimpleName():    " + Name.Inner.class.getSimpleName());

		System.out.println("args.getClass().getCanonicalName(): " + args.getClass().getCanonicalName());
		System.out.println("args.getClass().getName():          " + args.getClass().getName());
		System.out.println("args.getClass().getSimpleName():    " + args.getClass().getSimpleName());

	}
}
