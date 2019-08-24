package com.jadaaptive.domain;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.expr.IntegerLiteralExpr;

public class Generate {

	public static void main(String[] args) {
		CompilationUnit compilationUnit = new CompilationUnit();
		ClassOrInterfaceDeclaration myClass = compilationUnit
		        .addClass("MyClass")
		        .setPublic(true);
		myClass.addFieldWithInitializer(int.class, "A_CONSTANT", new IntegerLiteralExpr(43), Modifier.Keyword.PUBLIC, Modifier.Keyword.STATIC);
		myClass.addField(String.class, "name", Modifier.Keyword.PRIVATE);
		String code = myClass.toString();
		
		System.out.println(code);
		
		/*
		 * public class MyClass {

    		public static int A_CONSTANT;

    		private String name;
		}
		 */
	}
}
