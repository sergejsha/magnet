package test;

import magnet.Classifier;
import magnet.Scope;

import javax.annotation.Nullable;
import java.util.List;

@Scope
interface Scope1 {

    void bind1(String value);
    void bind2(@Classifier("bind2") String value);

    @Classifier("name3") String getName3();
    @Nullable @Classifier("name4") String getName4();

    List<String> getName5();
    @Classifier("name6") List<String> getName6();

}