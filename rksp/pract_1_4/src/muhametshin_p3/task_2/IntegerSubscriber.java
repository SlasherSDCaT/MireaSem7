package muhametshin_p3.task_2;

import io.reactivex.Observer;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.Disposable;

public class IntegerSubscriber implements Observer<Integer> {

    @Override
    public void onSubscribe(@NonNull Disposable disposable) {
        System.out.println(disposable.hashCode() + " has subscribed");
    }

    @Override
    public void onNext(Integer integer) {
        System.out.println("onNext method: " + integer);
    }


    @Override
    public void onError(Throwable throwable) {
    }

    @Override
    public void onComplete() {
        System.out.println("Emitting Complete!");
    }
}
