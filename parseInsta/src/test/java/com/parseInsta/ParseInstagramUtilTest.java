package com.parseInsta;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Created by shankar on 12/4/17.
 */
public class ParseInstagramUtilTest {

    @Test
    public void verifyAccessToken() {
        ParseInstagramUtil.initialize("3dcb0f9fa7d04e1da21d65c73971f9bb",
                "171a89a2134547bcbf27d2acd4fd4544",
                ":8888/parse");
    }

}