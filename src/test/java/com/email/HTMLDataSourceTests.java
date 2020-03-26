package com.email;

import com.email.Utils.HTMLDataSource;
import org.junit.Test;

import java.io.IOException;

public class HTMLDataSourceTests {

    @Test(expected = IOException.class)
    public void getInputStream_Null_Html_ThrowsException() throws IOException {
        HTMLDataSource dts = new HTMLDataSource(null);
        dts.getInputStream();
    }
}
