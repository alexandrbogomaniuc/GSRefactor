package com.abs.casino.filters;

import com.google.common.collect.ImmutableMap;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import javax.servlet.http.HttpServletRequest;

import static org.junit.Assert.assertArrayEquals;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class XssSanitizerWrapperTest {

    @Mock
    private HttpServletRequest request;

    @Test
    public void shouldNotFilterNullValues() {
        String[] param = new String[]{"value1", null, "value2"};
        when(request.getParameterMap()).thenReturn(ImmutableMap.of("param1", param));
        when(request.getParameterValues("param1")).thenReturn(param);
        assertArrayEquals(param, new XssSanitizerRequestWrapper(request).getParameterValues("param1"));
    }

    @Test
    public void shouldNotBreakMultiValueParams() {
        String[] param = new String[]{"good1", "good2", "<script>alert('1')</script>bad3"};
        when(request.getParameterMap()).thenReturn(ImmutableMap.of("param1", param));

        String[] expected = new String[]{"good1", "good2", "bad3"};
        assertArrayEquals(expected, new XssSanitizerRequestWrapper(request).getParameterValues("param1"));
    }

    @Test
    public void shouldSanitizeNestedXSS() {
        String[] param = new String[]{"xss<scr<script>alert('1')</script>ipt>alert('1')</script>"};
        when(request.getParameterMap()).thenReturn(ImmutableMap.of("param1", param));

        String[] expected = new String[]{"xssipt>alert('1')"};
        assertArrayEquals(expected, new XssSanitizerRequestWrapper(request).getParameterValues("param1"));
    }

    @Test
    public void shouldCatchBadSymbols() {
        String[] param = new String[]{"<svg\f\n\ronload=alert(1)>"};
        when(request.getParameterMap()).thenReturn(ImmutableMap.of("param1", param));

        assertArrayEquals(new String[]{"<svgonload=alert(1)>"}, new XssSanitizerRequestWrapper(request).getParameterValues("param1"));
    }
}
