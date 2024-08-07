# Ghi chú cho bản thân:

<details>
    <summary><span style="font-size: 25px; font-weight: 600">Java Reactive Programming</span></summary>

- <details>
  <summary>
      <b>Một số hiểu sai</b>
  </summary>

  - Reactive Programming không nhất thiết phải có **Asynchronous**, chúng ta có thể code **Synchronous**.

  </details>

- <details>
    <summary>
        <b>Use case</b>
    </summary>

  - **User events**: Đặc biệt là khi làm việc với các tác vụ bên phía UI, Front End. Khi user thực hiện một sự kiện
    nào đó thì cần thực hiện một **hành động** tương ứng cho sự kiện đấy.

  - **IO resposne**: Khi user thực hiện một input gì đấy chẳng hạn đọc file, sẽ có một luồng input diễn ra và sau khi
  đọc xong, cần thực hiện một **hành động** nào đó.
  </details>

- <details>
  <summary>
  <b>Tại sao lại cần quan tâm?</b>
  </summary>

  - <details>
    <summary><b>Câu hỏi</b></summary>

    Tại sao chúng ta lại phải quan tâm các vấn đề ở phần **Use case** khi mà đó là các việc xảy ra ở UI trong khi Java
    là ngôn ngữ được thực hiện đa số ở server-side?
    Quá trình hoạt động chủ yếu của server side là:

    - Nhận request đến.
    - Server thực hiện một số tác vụ.
    - Response dữ liệu.

    Trông có vẻ là **synchronous**? Chúng ta không bỏ ngang và làm một tác vụ gì khác, thế tại sao ta - developer
    back-end phải quan tâm đến reactive programming? Về cơ bản request phải **chờ request thực hiện xong** thì mới trả về client, đó là đặc trưng cơ bản của **HTTP**.
    </details>

  - <details>
    <summary><b>Yêu cầu của ứng dụng hiện đại</b></summary>
      Các ứng dụng hiện đại yêu cầu đến các vấn đề sau:

    - **High data scale** - dữ liệu truyền tải lớn.
    - **High usage scale** - số lượng người dùng lớn.
    - **Cloud based costs** - với sự bùng nổ của các giải pháp đám mây, hiện nay chúng ta thường thuê một dịch vụ
    lưu trữ bên thứ ba nên sẽ quan tâm đến vấn đề truyền tải hơn để tiết kiệm chi phí.
    </details>

  - <details>
     <summary>
      <b>Xem xét ví dụ & nhận ra vấn đề</b>
     </summary>

    **Ví dụ 1:** Vấn đề gì với đoạn code dưới đây?

    ```java
    @GetMapping("/users/{userId}")
     public User getUserDetails(@PathVariable String userId) {
         User user = userService.getUser(userId);
         UserPreferences prefs = userPreferencesService.getPreferences (userId);
         user.setPreferences (prefs);
         return user;
     }
    ```

    Đoạn code trên thực hiện hai thao tác:

    - a. Lấy user từ **User Service**.
    - b. Lấy user preferences từ **User Preferences Service**.

    Ta thấy hai thao tác này đang block lẫn nhau, thao tác `a.` cần phải diễn ra trước sau đó đến thao tác `b.` trong
    khi trên thực tế, hai thao tác này không hề phụ thuộc lẫn nhau => **Unnecessarily sequential**

    **Ví dụ 2:** Sơ đồ dưới đây thể hiện hoạt động của web server một cách khái quát nhất:
    ![example](images/Screenshot%202024-08-07%20192144.png)

    Về cơ bản thì:

    - Khi web server nhận được request, nó thêm một thread mới để handle request đó.
    - Sau đó một thread mới đến trong khi thread trước đó vẫn đang xử lí, web server sẽ spawn thêm một thread mới.
    - Nghĩa là, **một thread xử lí càng lâu** sẽ khiến cho **server có nhiều thread cùng tồn tại**.

    Ta thấy được đến một lúc nào đó, số lượng thread sẽ đạt giới hạn và server sẽ không thể spawn thêm thread mới => **Idling threads**
    </details>

  - <details>
    <summary>
    <b>Cách giải quyết - Old Concurrency APIs</b>
    </summary>

    Ta sử dụng các **Concurrency APIs** để giải quyết. Cụ thể là ta sẽ dùng hai class **Future** và **CompletableFuture** đã có từ Java 8:

    ```java
     CompletableFuture<User> userAsync = CompletetableFuture
         .supplyAsync(() => userService.getUser(userId));
    ```

    Vấn đề là khi chúng ta sử dụng nó trong SpringBoot sẽ khiến code của chúng ta trông rất lộn xộn như sau:

    ```java
    @GetMapping("/users/{userId}")
    public User getUserDetails(@PathVariable String userId) {
        CompletetableFuture<User> userAsync = CompletetableFuture.supplyAsync(() => userService.getUser(userId))
        CompletetableFuture<UserPreferences> userPreferencesAsync = CompletetableFuture.supplyAsync(() => userPreferencesService.getPreferences(userId))
        CompletetableFuture<Void> bothFutures = CompletetableFuture.allOf(userAsync, userPreferencesAsync)
        bothFutures.join()
        User user = userAsync.join();
        UserPreferences prefs = userPreferencesAsync.join();
        user.setPreferences(prefs);
        return user;
    }
    ```

    Ngoài ra, chúng ta cần phải thực hiện **tất cả các bước trên** chỉ để **hai tác vụ** được chạy **đồng thời**.

    Việc gọi hàm `userAsync.join()` vẫn sẽ khiến thread bị block, thread này vẫn cần phải **chờ cả hai tác vụ hoàn
    thành** thì sau đó mới return, bởi vì endpoint này return về **Object User**, thế nên thread phải đợi cả hai tác
    vụ trên hoàn thành để lấy được đầy đủ thông tin của User.

    Cách tiếp cận này **cải thiện** được việc hai tác vụ bây giờ sẽ **chạy song song** chứ không còn **chạy tuần tự**.
    Thế nhưng **thread vẫn bị block**.

    **Vấn đề tồn đọng**:

    1. Dev phải làm quá nhiều thứ.
    2. Error handling rất khó và lộn xộn.
    3. Về cơ bản vẫn là "tuần tự".

    **=> Cần giải pháp tốt hơn.**
    </details>

  - <details>
    <summary>
    <b>Cách giải quyết - Reactive Programming</b>
    </summary>

    Với việc sử dụng **Reactive Programming** chúng ta sẽ code như sau:

    ```java
     @GetMapping("/users/{userId}")
     public Mono<User> getUserDetails(@PathVariable String userId) {
         return userService.getUser(userId)
             .zipWith(userPreferencesService.getPreferences(userId))
                 .map(tuple -> {
                     User user = tuple.getT1();
                     user.setUserPreferences(tuple.getT2());
                     return user;
                 });
     }
    ```

    Sự khác biệt ở đây là gì?

    1. Code dễ đọc hơn trước đó.
    2. Dễ dàng thấy return type của method bây giờ không còn là `User` nữa mà được bọc trong một class `Mono<User>`.
    </details>

  </details>

- <details>
  <summary>
  <strong>Cách hoạt động</strong>
  </summary>

  Khi nói về **Reactive Programming**:

  - Thay đổi cách chúng ta nghĩ về **flow**.
  - Thay đổi cách chúng ta nghĩ về **data**.
  - Tương thích Java thông qua `Flow` interface từ Java 9.

  Lưu ý là: **Reactive Programming** không phù hợp với các dự án nhỏ.

  Để sử dụng Reactive, chúng ta cần có hiểu biết về **Collection Stream**.

  > **Java Stream Refresh**:
  > - Là một chuỗi các data.
  > - Chúng ta tập trung vào tính toán.
  > - Không quan tâm đến cách mà dữ liệu được lưu trữ trong một stream.
  > - Internal Iteration, chúng ta không chủ động thực hiện loop qua data.
  > - Một số operator phổ biến trong Stream: `map`, `filter`, `flatMap`, `findFirst`, ...

  ### Ý tưởng nền tảng:
  Có thể nói, Reactive chính là sự kết hợp của hai Design Pattern nổi tiếng khác là _Iterator Pattern_ và _Observer Pattern_:
  - Với Iterator Pattern ta có đoạn code như sau: 
    ```java
      myList.forEach(element -> System.out.println(element))
    ```
  - Với Observer Pattern ta có đoạn code như sau:
    ```java
      eventChannel.addObserver(event -> System.out.println(event))
    ```
  - Điểm khác biệt ở đây chỉ là **bên nào control** việc push data? Đối với *Iterator* thì đó là chính chúng ta, còn đối với *Observer* thì đó là Event hay Publisher.
  - Reactive là một "nỗ lực" để kết hợp hai hàm trên, một thứ gì đó tương tự thế này:
    ```java
      eventChannel
        .forEach(event -> event != null)
        .addOserver(event -> System.out.println(event))
    ```

  ### Khái niệm:
  Có thể giới thiệu ngắn ngọn Reactive = Asynchronous + Non-Blocking I/O (NIO), có nghĩa là một chương trình được gọi là Reactive nó sẽ đảm bảo được 2 yếu tố là Asynchronous (xử lý bất đồng bộ) và Non-Blocking I/O.

  Bằng cách viết những đoạn mã asynchronous và non-blocking, chương trình sẽ cho phép switch qua các tách vụ khác mà đang sử dụng cùng một I/O resource, và có thể quay lại sử lý tiếp khi tác vụ đó hoàn thành. Do đó với reactive programing chương trình có thể sử lý nhiều request hơn trên cùng một tài nguyên hệ thống.

  Reactive và non-blocking nhìn chung thì không làm cho ứng dụng chạy nhanh hơn. Lợi ích mà nó được kỳ vọng là ứng dụng chịu tải được tốt hơn mà chỉ yêu cầu ít tài nguyên hơn.

  ### Data Stream:
  Mỗi Stream sẽ emit ra ba thứ là: giá trị trả về (return data), lỗi (error) hoặc một tín hiệu hoàn thành (completed signal) nếu trong trường hợp ta không quan tâm tới giá trị trả về. Và cũng giống như Stream API trong Java 8, reactive stream sẽ không làm gì (không hoạt động) cho tới khi ta subscribe (lắng nghe) chúng. Hãy luôn ghi nhớ rằng: Không có gì xảy ra cho đến khi subscribe.
  
  </details>

</details>
