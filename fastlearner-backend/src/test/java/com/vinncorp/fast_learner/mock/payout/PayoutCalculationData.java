package com.vinncorp.fast_learner.mock.payout;

import jakarta.persistence.Tuple;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class PayoutCalculationData {

    public static final List<Tuple> instructorSalesTuples1 = new ArrayList<>();
    public static final List<Tuple> instructorSalesTuples2 = new ArrayList<>();
    public static final List<Tuple> instructorSalesTuples3 = new ArrayList<>();

    private void setInstructorSalesTuples1() {
        Tuple tuple1 = mock(Tuple.class);
        Tuple tuple2 = mock(Tuple.class);
        Tuple tuple3 = mock(Tuple.class);

        when(tuple1.get("instructor_id")).thenReturn(1L);
        when(tuple1.get("relative_seek_time")).thenReturn(0.05086114101184068893250);
        when(tuple1.get("paypal_email")).thenReturn("sb-dxxjx30818550@personal.example.com");

        when(tuple2.get("instructor_id")).thenReturn(2L);
        when(tuple2.get("relative_seek_time")).thenReturn(4.49031216361679224976250);
        when(tuple2.get("paypal_email")).thenReturn("sb-l47scc30344687@personal.example.com");

        when(tuple3.get("instructor_id")).thenReturn(6L);
        when(tuple3.get("relative_seek_time")).thenReturn(2.20882669537136706137250);
        when(tuple3.get("paypal_email")).thenReturn("");
    }
}
