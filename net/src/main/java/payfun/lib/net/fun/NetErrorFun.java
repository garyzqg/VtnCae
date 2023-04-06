package payfun.lib.net.fun;


import io.reactivex.Observable;
import io.reactivex.functions.Function;
import payfun.lib.net.exception.ExceptionEngine;

/**
 * @author : zhangqg
 * date   : 2019/11/20 9:32
 * desc   : <网络请求结果处理函数>
 */
public class NetErrorFun<T> implements Function<Throwable, Observable<T>> {

    @Override
    public Observable<T> apply(Throwable throwable){
        return Observable.error(ExceptionEngine.handleException(throwable));
    }

}
