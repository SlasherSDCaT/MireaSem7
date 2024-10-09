package muhametshin_p3.task_3;

import io.reactivex.Observable;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class UserFriend {
    private final int userId;
    private int friendId;
    public UserFriend(int userId, int friendId) {
        this.userId = userId;
        this.friendId = friendId;
    }

    public UserFriend(int userId) {
        this.userId = userId;
    }

    public int getUserId() {
        return userId;
    }

    public int getFriendId() {
        return friendId;
    }

    public void setFriendId(int friendId) {
        this.friendId = friendId;
    }

    public void addFriend(UserFriend friend) {
        this.friendId = friend.userId;
    }

    public static Observable<UserFriend> getFriends(int userId) {
        List<UserFriend> friends = new ArrayList<>();
        int n = new Random().nextInt(4) + 1;
        for (int i = 0; i < n; i++)
            friends.add(new UserFriend(userId, (int) (Math.random() * 100)));
        return Observable.fromIterable(friends);
    }

    public static void main(String[] args) {
        Integer[] userIdArray = {1, 2, 3, 4, 5};
        Observable<Integer> userIdStream = Observable.fromArray(userIdArray);
        Observable<UserFriend> userFriendStream = userIdStream.flatMap(userId -> getFriends(userId));

        userFriendStream.subscribe(userFriend -> {
            System.out.println("User: " + userFriend.getUserId() + ", Friend: "
                    + userFriend.getFriendId());
        });

    }

}
