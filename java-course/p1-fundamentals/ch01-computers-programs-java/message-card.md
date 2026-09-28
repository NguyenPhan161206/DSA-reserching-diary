# Ch01 — Computers, Programs, and Java

**Book ref:** Ch.1 (§1.1–1.12) · **Messages:** M1, M6 · **Size:** M
**Status:** 🟢 Done · **Prereq:** `../../../00-toolchain/` · **Last touched:** 2026-09-28

---

## 📌 3 câu hỏi chương này trả lời

1. Từ dòng text `.java` đến dòng lệnh CPU thực sự, có bao nhiêu lớp dịch? Ai làm việc gì?
2. Ngôn ngữ bậc cao đổi lấy cái gì, và đánh đổi bằng cái gì?
3. Tại sao "cùng một chương trình" có thể chạy nhanh gấp 10 lần giữa lần chạy đầu và lần chạy sau?

---

## 🧬 Messages

**M1-01 — `javac` không tạo ra chương trình, nó tạo ra bytecode; CPU không bao giờ thấy mã nguồn của bạn.**
`Ch01JitWarmup.java:24-27` là `total += i` — bốn dòng Java. `total` là `long`, `i` là
`int`, nên bytecode tương ứng không phải `iadd` mà là `i2l` + `ladd` + `lstore`
(kiểm bằng `javap -c` ngay dưới đây). Một lệnh cộng nguyên 32-bit là **một lệnh máy**;
nó chỉ trở thành vậy sau khi JIT dịch lần nữa.
- *Evidence:* `javap -c` trên `main()` (`total += i`):
  ```
  lload  8   ; total (long, 2 slots)
  iload  10  ; i (int)
  i2l        ; int -> long  (mixed-width add needs this)
  ladd       ; long + long
  lstore 8   ; total =
  ```
  và toàn bộ `main()` diễn giải theo mô hình stack (xem
  [`00-toolchain/README.md`](../../../00-toolchain/README.md) M1-01).
- *Counter-example:* `long + long` thuần là **một** `ladd`. Lệnh `i2l` xuất hiện không
  phải vì `long`, mà vì phép cộng **trộn độ rộng** (`int` + `long`). "Một phép toán =
  một lệnh máy" chỉ đúng với `int`, và thậm chí chỉ sau JIT.
- *Đổi gì trong đầu:* "Ngôn ngữ bậc cao nhanh hơn assembly" là sai. Nó **chậm hơn một
  bước**, và bước đó được trả bằng JIT.

**M1-02 — JVM không diễn giải bytecode cả đời. Nó biến bytecode nóng thành mã máy.**

```
$ ./run.sh lab/Ch01JitWarmup.java
batch              ms        ns/op
  ----------------------------------
1                5.58         2.79   <- cold
2                5.67         2.84
3                3.89         1.95
4                3.12         1.56
5                0.51         0.26
...
10               0.53         0.26

last batch is 10.6x faster than the first
```

Với `-Xint` (tắt JIT hoàn toàn):

```
1               17.96         8.98   <- cold
...
10              15.44         7.72

last batch is 1.1x faster than the first
```

- *Evidence:* **10,6×** giữa batch đầu và batch cuối khi có JIT; **1,1×** khi không. Và
  batch cuối của `-Xint` (7,72 ns) chậm hơn batch **đầu tiên** của chế độ thường (2,79 ns)
  gần **3 lần**.
- *Counter-example:* cùng file `.class` đó, khác cờ dòng lệnh là khác tốc độ 30 lần.
  Bytecode không đổi một byte nào.
- *Đổi gì trong đầu:* "Java chậm hơn C++" chỉ đúng ở **thời điểm khởi động và 10⁴ vòng
  lặp đầu**. Sau đó không còn. Đây là lý do benchmark viết bằng Java mà quên warm-up là
  một con số bịa đặt.

**M2-01 — "Kiểu dữ liệu đúng" có một giá cố định, và JVM công bố nó.**
`Ch01TypesAndBoxing.java` in ra `Byte.BYTES=1 … Long.BYTES=8`, và `int` tràn thành
`-2147483648` khi cộng 1. Giá của `int` là 4 byte mà bạn **không** phải trả cho
`short`.
- *Evidence:* dòng kích thước trong output lab.
- *Counter-example:* `boolean` **không** có hằng `BYTES`. JLS §4.2 chỉ hứa **1 bit** cho
  boolean *trong mảng và trường bit*, còn HotSpot dành trọn **1 byte** cho mỗi trường
  boolean của object. Câu "mọi kiểu đều có kích thước hằng số" hỏng ngay tại `boolean` —
  và "boolean tốn 1 bit" cũng sai khi nói về trường của object.
- *Đổi gì trong đầu:* Chọn `int` chứ không phải `short` **không phải** là "tiện tay" — nó
  là trao đổi 2 byte heap lấy việc tránh hàng nghìn lần ép kiểu.

**M2-02 — Boxing không tốn thời gian nhiều như sách tưởng. Nó tốn bộ nhớ — 12,5 lần.**

```
  int[]    sum=5000000     6.5 ms  (8,000,000 bytes, 1 allocation)
  fill boxed: data[0] == data[4,999,999]: true   (one shared object, 20,000,016 bytes)
  Integer[] sum=12502557925984    12.2 ms  (100,000,000 bytes, 5,000,000 allocations)
```

5.000.000 phần tử. Thời gian chỉ chậm hơn **1,9×**; bộ nhớ tăng **12,5×**.
- *Evidence:* ba dòng trên. Dòng **giữa** là phát hiện phụ mà bản thân tôi từng hiểu sai:
  `Arrays.fill(data, 1)` gán **một reference duy nhất** cho mọi slot — con số 5.000.000
  allocation trong `Integer[]` chỉ đúng khi mỗi slot nhận một object khác nhau (đây là
  thí nghiệm đo thật ở dòng cuối, giá trị nằm ngoài cache để autobox không tái dùng).
- *Counter-example:* nếu vòng lặp cộng vào một `List<Long>` bằng `add()` thay vì cộng vào
  `long` cục bộ, mọi boxing sẽ tạo thêm một allocation cho kết quả → bộ nhớ bắt đầu
  chạy GC. Đo lại sẽ thấy chênh lệch thời gian nhảy lên hàng chục lần.
- *Đổi gì trong đầu:* Câu "boxing là khoản thuế $O(n)$ ẩn" trong ghi chú DSA là **nửa
  đúng nếu nói về bộ nhớ, sai nếu nói về thời gian**. Ở đây JIT unbox ngay trong vòng
  lặp nên thời gian gần như không đổi. Đừng lý giải bằng thứ không đo được.

**M1-03 — `==` trên `Integer` phụ thuộc vào cache, nên một test có thể xanh rồi hỏng.**

```
  Integer.valueOf(1) == Integer.valueOf(1): true     (cache -128..127)
  Integer.valueOf(1000) == Integer.valueOf(1000): false
```

- *Evidence:* hai dòng trên. Cache là một tối ưu bộ nhớ của `Integer.valueOf`, **không phải
  một quy tắc của ngôn ngữ**. `==` trên wrapper luôn là so sánh *tham chiếu*, đúng và sai
  tuỳ giá trị có nằm trong cache hay không.
- *Counter-example:* `Integer i = 127; i == 127` → `true`. `Integer i = 1000; i == 1000` →
  `false`. Một đoạn code chạy đúng ở bộ test nhỏ vỡ ở dữ liệu thật.
- *Đổi gì trong đầu:* "Nó chạy được mà" không phải bằng chứng. Kiểm chứng = chạy với
  giá trị **ngoài** vùng cache.

---

## ⚠️ Anti-messages

| The trap | What actually happens | Repro |
|----------|----------------------|-------|
| "Java chậm hơn C++ vì diễn giải" | JIT dịch nóng sau ~10⁴ lần gọi; đo ở batch 1 cho tỉ lệ sai 10× | `Ch01JitWarmup` |
| "Mã Java chạy giống nhau mỗi lần" | `-Xint` cho 30 lần chậm hơn với **cùng file `.class`** | `java -Xint` |
| "Dùng `Integer` thì chậm hơn" | Chậm 1,9× khi **mỗi slot một object thật** (đo mới); nguyên nhân là **100 MB vs 8 MB**, không phải boxing đắt | `Ch01TypesAndBoxing` |
| "Mọi kiểu đều có hằng `BYTES`" | `Boolean.BYTES` **không tồn tại**; và boolean trong object field tốn 1 byte, không phải 1 bit | `javap java.lang.Boolean` |
| "boolean tốn 1 bit" | Đúng trong mảng/bit field theo JLS, **sai** cho trường của object trên HotSpot | `Ch01TypesAndBoxing` |
| "Tràn số sẽ báo lỗi" | `Integer.MAX_VALUE + 1 == Integer.MIN_VALUE`, im lặng | `Ch01TypesAndBoxing` |

---

## 🆕 Java 21 delta vs sách

| Book | Java 21 | Why it matters here |
|------|---------|--------------------|
| `new Integer(1)` | **Deprecated for removal** — `javac` cảnh báo `[removal]`. Phải dùng `Integer.valueOf(1)` | Chương 10 viết `new Integer(...)`; biên dịch với `-Werror` sẽ dừng |
| `boolean` là 1 byte trong bảng | JLS §4.2 cho phép 1 bit **trong mảng và bit field**; HotSpot vẫn dùng 1 byte cho trường object, nên không có hằng `Boolean.BYTES` để hỏi | Bài toán "boolean tốn bao nhiêu byte" không có câu trả lời duy nhất |
| "JIT là chi tiết cài đặt" | Vẫn vậy, nhưng **JIT hiện dịch theo profile** (type profiling), không chỉ theo điểm nóng | Giải thích vì sao `Integer[]` unbox được: vòng lặp chỉ thấy một kiểu cụ thể |
| `-Xint` nhắc trong sách | Vẫn dùng được, nay còn `-XX:-TieredCompilation`, `-XX:CompileThreshold=` | Dùng để **chứng minh** M1-02 thay vì tin lời |
| Không nhắc AOT | `jaotc` đã bị bỏ; native-image (GraalVM) là hướng thay thế | Startup time là vấn đề thật của JVM, và là lý do Docker image `jre` vs `jdk` khác nhau |

---

## 🔗 Cầu nối

- **Previous:** [`00-toolchain/README.md`](../../../00-toolchain/README.md) — `javap` output
  và benchmark đầu vào, M1-01/M1-03 ở đó.
- **Next:** [`ch02-elementary-programming/`](../ch02-elementary-programming/) — M2-01
  (tràn số) và M2-02 (boxing) được xử lý triệt để.
- **To Ch 5:** [`ch05-loops/`](../ch05-loops/) — nếu hiểu vì sao batch 1 chậm, hiểu vì sao
  "tối ưu hoá vòng lặp" của trình biên dịch là quy trình nhiều bước.
- **To Ch 32:** JIT nóng lên là mô hình mental chính xác cho `ForkJoinPool` — chia nhỏ
  công việc để đủ "nóng".
- **Diary note:** [reaserching-diary → software_architecture](https://github.com/NguyenPhan161206/reaserching-diary/tree/main/software_architecture)
  — JVM startup vs native là một quyết định kiến trúc, không phải chi tiết triển khai.
- **Lab:** `lab/Ch01JitWarmup.java` (chạy 2 lần: thường rồi `-Xint`),
  `lab/Ch01TypesAndBoxing.java`.

---

## 🧪 Kết quả lab (output thật)

```
--- fixed sizes (from the JVM, not from the book) ---
  Byte.BYTES=1  Short.BYTES=2  Character.BYTES=2  Float.BYTES=4
  Integer.BYTES=4  Long.BYTES=8  Double.BYTES=8
  Boolean has no BYTES constant. The JLS (4.2) only promises 1 bit
  for booleans inside arrays and bit fields; HotSpot reserves a whole
  byte per boolean field. Do not assume 1 bit on an object field.
  object header on a 64-bit JVM: 16 bytes (12 with compressed oops)
  Integer.valueOf(1) == Integer.valueOf(1): true   (cache -128..127)
  Integer.valueOf(1000) == Integer.valueOf(1000): false  (outside the cache)

--- ranges ---
  byte   -128 .. 127
  short  -32768 .. 32767
  int    -2147483648 .. 2147483647
  long   -9223372036854775808 .. 9223372036854775807
  int  overflow: MAX_VALUE + 1 = -2147483648

--- 5,000,000 elements ---
  int[]    sum=5000000     6.5 ms  (8,000,000 bytes, 1 allocation)
  fill boxed: data[0] == data[4,999,999]: true   (Arrays.fill shares one object)
  Integer[] sum=12502557925984    12.2 ms  (100,000,000 bytes, 5,000,000 allocations)
```

> **Chú thích sửa đổi (2026-09-28):** output ở trên được đo lại sau khi sửa lỗi nội dung
> trong `Ch01TypesAndBoxing.java`. Bản cũ tuyên bố `Arrays.fill(data, 1)` tạo 5.000.000
> allocation — **sai**: `Arrays.fill` chép cùng một reference, và `1` nằm trong cache nên
> cả mảng trỏ tới một object. Con số 5.000.000 allocation ở dòng `Integer[]` giờ là đo
> thật (mỗi slot một object ngoài cache). Toàn bộ chương trình đã chuyển mục tiêu: đo
> đúng, không minh hoạ cho khẳng định có sẵn.

---

## ❌ What I Got Wrong

- **`Boolean.BYTES` không tồn tại.** Tôi viết `printf` với nó vì "mọi wrapper đều có
  `BYTES`" — đúng với 6 cái, sai với `Boolean`. Tôi đã khẳng định điều mình chưa kiểm
  chứng, và `javac` bắt được ngay. **Bài học về quy trình:** một dòng in kích thước kiểu là
  một dòng lệnh cần chạy, không phải một dòng để tin.
- **Tôi sửa nó bằng một khẳng định sai khác.** Viết rằng "JVM lưu boolean là 1 bit".
  JLS §4.2 chỉ nói 1 bit cho boolean trong mảng và bit field; HotSpot dành 1 byte cho
  trường object. Tôi đã thay một tuyên bố chưa kiểm chứng bằng một tuyên bố chưa kiểm
  chứng khác, chỉ vì nó nghe hợp lý hơn. **Đây là lỗi nguy hiểm hơn lỗi ban đầu** — lần này
  nó sẽ sống lâu hơn trong ghi chú vì nó không còn bị `javac` bắt.
- **`new Integer(1)` bị deprecation.** Tôi dùng nó vì sách dùng nó. `javac` cảnh báo
  `[removal]`. Tôi đã ghi vào mục delta, nhưng lẽ ra phải nghi ngờ ngay khi thấy cảnh báo
  trình biên dịch — cảnh báo trình biên dịch là dữ liệu, không phải nhiễu.
- **Tôi đoán boxing "chậm hơn nhiều" trước khi đo.** Đo ra 1,3×. Câu này đã tồn tại trong
  ghi chú `02-java-for-dsa.md` của diary dưới dạng khẳng định. Nó cần được sửa thành "tốn
  bộ nhớ 12,5×", hoặc đo lại với `List<Long>` + `add()` để xem khi nào thời gian mới bị ảnh
  hưởng. → Đã ghi vào `Open questions` diary để user quyết định sync.
- **`total += i` tôi khẳng định là `iadd`.** `total` là `long`, `i` là `int`: bytecode thật
  (chạy `javap -c`) là `i2l` + `ladd`, không có `iadd`. Tôi đã viết "một lệnh cộng" cho
  thứ hoá ra là **ba** lệnh mỗi chu kỳ, và counter-example trong sách tôi ghi lại còn sai
  nặng hơn ("`long`+`long` cần `iadd`+`i2l`+`ladd`" — `long`+`long` là một `ladd`; `i2l`
  chỉ xuất hiện khi **trộn độ rộng**). Lý thuyết JVM của tôi vẫn là "bài toán trong đầu",
  chưa phải thứ đọc được từ bytecode. Cố tật cần bỏ: khẳng định bytecode mà chưa chạy
  `javap -c` (chạy `javap -c -p` trên `.class` sau `run.sh` để tự kiểm).
- **`Arrays.fill(data, 1)` tôi tin là 5.000.000 object, kèm comment "note: not the
  cached instance".** Cả hai đều sai: `fill` chép cùng reference, và `1` là cached
  instance. Lab cũ in "(100,000,000 bytes, 5,000,000 allocations)" cho dữ liệu thật chỉ
  có **một** object — lỗi này sống lâu vì nó nằm trong **output in ra**, cái mà mọi
  người coi là bằng chứng. Bằng chứng tự in bằng chứng giả thì tệ hơn không có bằng
  chứng.

---

## 🤔 Câu hỏi mở

- [ ] Với `-Xint`, batch đầu (8,98 ns) chậm hơn batch 5 (7,77 ns) — vì sao có bậc thang
      nếu không có JIT? Đo `-XX:TieredStopAtLevel=1` để tách C1 khỏi C2.
- [ ] `Integer[]` chỉ chậm ~1,9× vì JIT unbox trong vòng lặp. Nếu vòng lặp xử lý **hai**
      kiểu khác nhau qua generics, unboxing còn được tối ưu không? (link Ch 19 — type erasure)
- [ ] `-XX:MaxRAMPercentage` mặc định là bao nhiêu, và tại sao `-Xmx` thấp lại làm
      `OutOfMemoryError: Java heap space` ở chỗ không ngờ? (link Ch 32)
- [ ] Viết lại `Ch01TypesAndBoxing` để **đo** allocation bằng `ThreadMXBean`
      (`getThreadAllocatedBytes`) thay vì suy ra từ vòng lặp — con số 5.000.000 hiện tại
      là một phép lý luận (mỗi `Integer` ngoài cache một object), chưa phải số đếm thật.
