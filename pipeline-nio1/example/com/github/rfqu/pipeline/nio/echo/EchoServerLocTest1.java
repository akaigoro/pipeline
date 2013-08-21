package com.github.rfqu.pipeline.nio.echo;

import com.github.rfqu.pipeline.nio.echo.EchoServerLocTest;

/**
 *  Runs tests with {@EchoServer2} launched in the same JVM.
 *  Can be run as Junit tests or as java application.
 */
public class EchoServerLocTest1 extends EchoServerLocTest {

    public static void main(String[] args) throws Exception {
        EchoServerLocTest.main(args);
    }

}
