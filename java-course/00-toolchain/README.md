# 00 — Toolchain

**Gate:** this stage. Every later chapter assumes you can compile, run, read a stack
trace, measure, and run a test in one command.
**Messages:** M1, M2, M6
**Status:** 🟢 Done · **Last touched:** 2026-09-28

---

## 📌 3 câu hỏi stage này trả lời

1. Java code của bạn thực sự được *biến đổi* thành cái gì trước khi chạy?
2. Khi chương trình hỏng, dấu vết đó cho tôi biết **bao nhiêu** về chương trình?
3. Làm sao tôi biết một bài kiểm thử thực sự đang *canh giữ*?

---

## 🧬 Messages

**M1-01 — `javac` không tạo ra "chương trình", nó tạo ra *bytecode*.**
`javac` biên dịch sang `.class`, rồi `java` (JVM) mới thực thi `.class`.

```
$ javac Hello.java
$ ls Hello.class
Hello.class
$ javap -c -p Hello.class | sed -n '/main/,/return/p'
  public static void main(java.lang.String[]);
    Code:
       0: getstatic     #7    // Field java/lang/System.out:Ljava/io/PrintStream;
       3: ldc           #13   // String Hello, Java 21
       5: invokevirtual #15   // Method java/io/PrintStream.println:(Ljava/lang/String;)V
       8: return
```

Mệnh đề kiểm chứng được: một lệnh `System.out.println` **4 dòng** mã nguồn → **3 lệnh**
bytecode. Biến `String` không tồn tại ở tầng bytecode — nó là một hằng trong pool.
Nếu bytecode có bước `ldc` thì chuỗi nằm trong **constant pool của class**, không phải
trên heap.
→ Đây là lý do `==` so sánh *địa chỉ* mà không phải *nội dung*: ở tầng JVM, nó là so
sánh hai con trỏ. Sẽ đào sâu ở Ch 4.

**M1-02 — Stack trace là dữ liệu, không phải tiếng khóc.**
Mỗi dòng `at ...` là **một frame còn sống** trên thread stack tại thời điểm ném, đọc từ
trong ra ngoài.

```
$ ./run.sh lab/StackTraceDemo.java
caught: java.lang.NullPointerException
--- frame walk ---
  at StackTraceDemo.thirdLayer(StackTraceDemo.java:11)   ← ném ở đây
  at StackTraceDemo.secondLayer(StackTraceDemo.java:15)
  at StackTraceDemo.firstLayer(StackTraceDemo.java:19)
  at StackTraceDemo.main(StackTraceDemo.java:24)         ← nơi bắt
```

Hai điều kiểm chứng được từ output này:
- Biến `data` được khai báo ở dòng 10 nhưng ném ở dòng 11 → **null check không tồn tại
  trong Java**. Đây là hệ quả của M2 (không có type nào chứa `null`).
- Frame đầu là nơi *ném*, frame cuối là nơi *bắt*. **Số frame = độ sâu call stack** —
  con số này chính là thứ giới hạn đệ quy ở Ch 18.

**M1-03 — Cách đọc input là một phần của thuật toán.**
Ba bộ đọc, **cùng một việc**: đọc $N$ số nguyên và trả tổng. Chỉ khác cách byte thành giá trị.
Đo trên 1.000.000 token, `best-of-2`, cùng một JVM:

| Bộ đọc | Thời gian | So với nhanh nhất |
|---------|-----------|-------------------|
| `Scanner.nextInt()` | 384 ms | **52,6×** |
| `BufferedReader` + parse thủ công | 34 ms | 4,7× |
| `FastScanner` (byte → `int`) | **7 ms** | 1,0× |

```
$ ./run.sh lab/InputBenchmark.java 1000000
tokens: 1,000,000   input size: 3,890,000 bytes
expected sum: 499500000
```

Nguyên nhân phân giải được, không phải "Scanner chậm": `Scanner` **cấp phát một
`String` cho mỗi token** rồi khớp bằng biểu thức chính quy. `FastScanner` không tạo
`String` nào — nó dựng giá trị trực tiếp trong `int`. 3,9 MB đầu vào biến thành 1 triệu
object rác.
→ Nếu đề yêu cầu $O(n)$ mà bạn viết `$O(n^2)$ vì `ArrayList.contains()` trong vòng lặp,
đây là cùng một lớp lỗi: **cách biểu diễn quyết định hằng số, và hằng số quyết định
kết quả chấm điểm.**

**M1-04 — JDK ≠ JRE. Compiler là công cụ build, không phải thư viện runtime.**
Máy này ban đầu chỉ có `java` (JRE 21.0.11), `javac` không tồn tại → **không biên dịch
được gì**. Bộ JDK 21.0.12.1 đặt ở `~/.local/jdk21` (không cần root).
Điều này có hệ quả thực tế trên Docker/CI: image `eclipse-temurin:21-jre` chạy được
ứng dụng nhưng **không build được** ứng dụng.

**M2-01 — Kiểu dữ liệu bắt lỗi sớm, nhưng không phải lỗi nào.**

```
jshell> double a = 0.1 + 0.2; System.out.println(a);
0.30000000000000004          ← 0.1 và 0.2 không biểu diễn chính xác trong nhị phân
jshell> a == 0.3
$3 ==> false
jshell> int x = Integer.MAX_VALUE; x + 1
$5 ==> -2147483648           ← tràn số KHÔNG ném lỗi, nó quay vòng
```

Ba dòng này là **toàn bộ** chương 2, 3 và 4 nằm gọn. `double` sai về mặt toán học,
`int` sai về mặt số học, và cả hai đều biên dịch sạch.

**M6-01 — Bộ test chưa từng đỏ thì chưa chứng minh điều gì.**
`mvn test` trên `Triangle.area()` có lỗi biên `<` thay vì `<=`:

```
Tests run: 14, Failures: 11
TriangleTest.explicitValues:43 expected: <1> but was: <0>
TriangleTest.explicitValues:43 expected: <6> but was: <3>
TriangleTest.explicitValues:43 expected: <10> but was: <6>
TriangleTest.explicitValues:43 expected: <55> but was: <45>
TriangleTest.matchesClosedForm:36 expected: <5050> but was: <4950>
```

Sửa một ký tự → `Tests run: 14, Failures: 0` / `BUILD SUCCESS`.

Bài học thật nằm ở chỗ **test nào vẫn xanh khi code còn lỗi**: `area(0)` trả về `0`
đúng với cả hai phiên bản, và `assertThrows(IllegalArgumentException)` cũng xanh ngay
từ đầu. Một bộ test chỉ toàn trường hợp biên bằng `0` sẽ báo xanh trên code sai.
→ Đây là lý do `0, 1, 2, 3, 4, 10, 100` nằm trong `@ValueSource` chứ không phải `{0, 100}`.

---

## 🆕 Java 21 delta so với bản in

| Nội dung sách | Java 21 |
|---------------|---------|
| `Scanner`, `BufferedReader` | Vẫn đúng ở Java 21. (Bản cũ của dòng này viết "giờ có `java.io.IO.read`" — **sai cả hai cách**: `java.io.IO` chỉ xuất hiện như preview ở JDK 23 và hoá thành `java.lang.IO` ở JDK 25, và nó có `readln()`/`println()`, không có `read()`. Với trình chấm, bài học ở phần *Input benchmark* vẫn giữ nguyên: `FastScanner` nhanh hơn `Scanner` 52×) |
| `System.out.println` | Sẽ dạy `printf`, `formatted`, text block (`""`) |
| JUnit 4 (`@Test` của `org.junit`) | **JUnit 5**: `org.junit.jupiter`, `@BeforeEach` thay `@Before` |
| `Integer` boxing | `int → Integer` vẫn có nhưng `List.of`/`List.copyOf` thay cho mảng cố định |
| JavaFX gọi chung "chương 14–16" | **Không còn trong JDK từ 11** → phải thêm dependency `org.openjfx` qua Maven |
| `Thread` / thread pool (Ch 32) | **Virtual thread** (`Thread.ofVirtual()`) — phần lớn bài học về thread pool đã lỗi thời |
| `URL.openStream()` cho web crawler (Ch 12) | Thay bằng `java.net.http.HttpClient` |

---

## 🔧 Cách dùng

```bash
# 1. một lần mỗi terminal
source java-course/00-toolchain/env.sh

# 2. chạy một lab bất kỳ trong cả khoá học
./java-course/00-toolchain/run.sh java-course/00-toolchain/lab/InputBenchmark.java 1000000
./java-course/00-toolchain/run.sh java-course/00-toolchain/lab/       # cả thư mục

# 3. test
cd java-course/00-toolchain/junit-demo && mvn test
```

`run.sh` biên dịch vào `.java-course-build/` (đã git-ignore) — repo **không bao giờ**
chứa file `.class`.

---

## 📁 Artefact

| File | Vai trò |
|------|---------|
| `env.sh` | Đưa `javac` / `jshell` / `mvn` vào `PATH` |
| `run.sh` | Compile + run bằng **một lệnh**, dùng được cho mọi chương sau này |
| `lab/Hello.java` | Chương trình đầu tiên + nguồn cho `javap` ở M1-01 |
| `lab/StackTraceDemo.java` | M1-02 — đọc stack trace như dữ liệu |
| `lab/FastScanner.java` | Thư viện copy-paste, không có `main()` |
| `lab/InputBenchmark.java` | M1-03 — đo được, không phải đoán |
| `junit-demo/` | M6-01 — một test đỏ thật, rồi xanh thật |

---

## ❌ What I Got Wrong

- **Lần chạy JUnit demo đầu tiên tôi thiết kế contract sai:** `area(3)` trả về `6` dưới
  cả code lỗi lẫn code đúng, vì tôi chọn `row <= size` bắt đầu từ `row = 0` — số `0` cộng
  vào không đổi giá trị. Test "đỏ" hoá ra xanh. Phải đổi sang lỗi biên `<`/`<=` ở đầu
  vòng lặp thì lỗi mới lộ ra được ở nhiều giá trị đầu vào.
  **Bài học về quy trình:** một test đỏ phải đỏ ở **nhiều** dữ liệu, không phải đỏ ở đúng
  một cái may mắn. Nếu chỉ một assertion đỏ, hãy hỏi: *test này có thật sự bắt được lỗi
  không, hay chỉ không trùng số?*
- **Tôi định dùng `sudo` để cài JDK.** Không cần — kéo tarball về `~/.local/` an toàn
  hơn, không sửa hệ thống, gỡ được bằng cách xoá thư mục.

---

## 🔗 Cầu nối

- Chương 1 (`p1-fundamentals/ch01-*/message-card.md`) — mở rộng M1-01/M1-04 thành
  câu chuyện đầy đủ về bytecode và JVM.
- Chương 4 — `FastScanner` chính là hệ quả của việc hiểu `char`/`int` và escape.
- Chương 44 — `junit-demo/` là tiền đề; note đầy đủ ở `p5-advanced/ch44-*/`.
- `solutions/01-fundamentals/` — mọi bài cần nhập $10^6$ token dùng lại `FastScanner`.

## 🤔 Câu hỏi mở

- [ ] `-Xss` (Java stack) và `ulimit -s` (OS stack) khác nhau thế nào? Kiểm bằng cách
      đệ quy sâu cho tới khi ném `StackOverflowError`.
- [ ] Vì sao `Scanner` chậm hơn `BufferedReader` ~11×, trong khi `BufferedReader` chậm
      hơn `FastScanner` ~5×? Đo riêng phần parse để tách chi phí I/O và chi phí parse.
- [ ] `-Xint` (không JIT) thay đổi tỉ lệ 52× này thế nào? Nếu tỉ lệ giữ nguyên thì
      tổng phí là I/O hay là allocation?
