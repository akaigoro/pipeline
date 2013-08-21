package com.github.rfqu.pipeline.nio.echo;

import com.github.rfqu.pipeline.nio.echo.EchoServerLocTest;

/**
 *  Runs tests with {@EchoServer1} launched in the same JVM.
 *  Can be run as Junit tests or as java application.
 */
public class EchoServerLocTest2 extends EchoServerLocTest {

    public static void main(String[] args) throws Exception {
        EchoServerLocTest.main(args);
    }

}
