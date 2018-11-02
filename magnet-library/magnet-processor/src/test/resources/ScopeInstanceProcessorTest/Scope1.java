package test;

import magnet.Scope;
import magnet.Classifier;

import javax.annotation.Nullable;
import java.util.List;

@Scope
interface Scope1 {

    String getName1();
    @Classifier("name2") String getName2();
    @Nullable String getName3();
    @Nullable @Classifier("name4") String getName4();
    List<String> getName5();

    void bind1(String value);
    void bind2(@Classifier("hello") String value);
}