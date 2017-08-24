package com.xailan.study.java.annotation.pizza.factory.processor;

import javax.lang.model.element.Element;

public class ProcessingException extends Exception {

	private static final long serialVersionUID = 1036791878161059520L;
	
	Element element;

	public ProcessingException(Element element, String msg, Object... args) {
		super(String.format(msg, args));
		this.element = element;
	}

	public Element getElement() {
		return element;
	}
}
