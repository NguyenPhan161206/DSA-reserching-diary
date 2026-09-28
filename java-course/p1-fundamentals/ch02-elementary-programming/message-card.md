# Ch02 — Elementary Programming

**Book ref:** Ch.2 (§2.1–2.19) · **Messages:** M1, M2 · **Size:** L
**Status:** 🟢 Done · **Prereq:** [`ch01-computers-programs-java/`](../ch01-computers-programs-java/) · **Last touched:** 2026-09-28

---

## 📌 3 câu hỏi chương này trả lời

1. Vì sao `int` và `double` đều biểu diễn "số", nhưng lại có hai bộ quy tắc hoàn toàn khác nhau?
2. Khi nào việc ép kiểu chỉ làm mất một chút chính xác, và khi nào nó phá hỏng hoàn toàn giá trị?
3. Một chương trình đọc dữ liệu có thể **đúng trên máy tôi** và **sai trên máy khác**. Vì sao?

---

## 🧬 Messages

**M2-01 — `int` và `double` không phải hai phiên bản của cùng một thứ. Chúng là hai kiểu với hai luật khác nhau.**

```
Integer.MAX_VALUE          = 2147483647
Integer.MAX_VALUE + 1      = -2147483648      ← tràn, im lặng
1_000_000_000 * 3 (int)    = -1294967296      ← sai hoàn toàn
1_000_000_000L * 3 (long)   = 3000000000       ← đúng
(float) 16_777_217         = 16777216         ← mất 1 đơn vị
(double) 16_777_217        = 16777217         ← chính xác
```

`int` là **số nguyên có thứ tự, 32 bit, quay vòng khi tràn**. `double` là **xấp xỉ 53
bit có dấu, không bao giờ tràn — chỉ làm tròn**. Ba dòng đầu và hai dòng cuối không thể
suy ra từ nhau.
- *Evidence:* 5 dòng trên.
- *Counter-example:* `float` **tệ hơn** `int` ở đây: `int` giữ chính xác tới 2³¹, `float`
  chỉ giữ 24 bit. Câu "dùng kiểu lớn hơn cho chắc" sai — lớn hơn phải là *sau*, không phải
  *thay vì*.
- *Đổi gì trong đầu:* Câu hỏi khi chọn kiểu không phải "kiểu nào đủ lớn" mà là **"phép
  toán nào sẽ chạy trên nó"**. `int` không bao giờ trừng phạt, nó chỉ im lặng quay vòng.

**M2-02 — Nới rộng miễn phí, thu hẹp phải ép kiểu, và thu hẹp không bao giờ báo lỗi.**

```
byte 100 -> int 100 -> long 100 -> double 100.0
2^53+1 as long then as double: 9007199254740993 -> 9007199254740992  (lost!)
(int) 1e+20 = 2147483647
```

`(int) 1e20` cho `Integer.MAX_VALUE` — **bão hòa**, không phải phản chiếu bit, và không ném
gì cả. Nếu bạn viết `(int)(a / b)` với `b == 0.0` bạn nhận `0`, không phải `NaN`.
- *Evidence:* 3 dòng trên.
- *Counter-example:* `9007199254740993` là số **mất đúng 1 đơn vị** khi qua `double`. Không
  ai nhìn thấy. Đây là hàng của bài toán timestamp/tiền tệ ở mốc $2^{53}$.
- *Đổi gì trong đầu:* "Ép kiểu an toàn vì tôi đã yêu cầu nó" — yêu cầu ép kiểu chỉ chứng
  minh rằng **bạn đã nghĩ tới việc mất dữ liệu**, không phải rằng nó không xảy ra.

**M2-03 — `b += 1` biên dịch được trên `byte`; `b = b + 1` thì không. Một trong hai là quy tắc ngôn ngữ, cái kia là tiện lợi của trình biên dịch.**

```
byte b = 127; b += 1;  ->  b == -128
```

JLS định nghĩa `b += 1` **chính là** `b = (byte)(b + 1)` — ép kiểu được chèn thêm vào
chính vì mệnh đề gán. Đây là trường hợp hiếm mà ngôn ngữ **chủ động làm mất dữ liệu** để
giữ ngắn gọn.
- *Evidence:* dòng trên; `b = b + 1` là **compile error**.
- *Counter-example:* cùng một dòng `b += 1` ấy, nếu `b` là `int` thì không hề có vấn đề.
  Cái nguy hiểm là *cùng cú pháp, hai hành vi* — và người mới nhìn thấy `+=` ở khắp nơi
  nên không hề nghi ngờ.
- *Đổi gì trong đầu:* Khi gặp hành vi "kỳ lạ nhưng biên dịch vẫn qua", phải hỏi **"ai vừa
  chèn ép kiểu?"** chứ không hỏi "tại sao nó biên dịch được".

**M4-01 — `Scanner` đọc dữ liệu theo **locale của JVM**, nên cùng một byte có thể đọc
thành hai số khác nhau.**

```
default            locale=US      nextDouble("12,5") -> InputMismatchException
forced to Germany  locale=DE      nextDouble("12,5") = 12,5
```

Không phải "chậm" (đã đo ở toolchain: 52× so với `FastScanner`) — mà là **đọc sai**.
`Locale.getDefault()` trên máy dev có thể là `vi_VN`, `de_DE`, `tr_TR`... và khi đó `12.5`
trong đề bài bị đọc thành `125` hoặc ném `InputMismatchException`.
- *Evidence:* 2 dòng trên.
- *Counter-example:* trường hợp xấu nhất không phải là đọc sai mà là **đọc đúng trên máy
  bạn, sai trên judge** — bạn không bao giờ thấy lỗi.
- *Đổi gì trong đầu:* Mọi chương trình đọc chuỗi số phải **chốt locale một cách tường minh**:
  `new Scanner(System.in).useLocale(Locale.US)`, hoặc dùng `FastScanner` vốn parse theo byte.

**M4-02 — `nextLine()` sau `nextInt()` trả về chuỗi rỗng, và dữ liệu người dùng biến mất âm thầm.**

```
age=25 weight=67.5 height=4.5
nextLine() returned []  (length 0)
```

`nextInt()` dừng **ngay trước** `\n` mà không nuốt nó; `nextLine()` nuốt phần đầu của dòng
(tức là chỉ có `\n`) rồi trả về `""`.
- *Evidence:* dòng trên — độ dài 0, không phải chuỗi sai.
- *Counter-example:* nó chỉ hỏng khi người dùng nhập **cả số lẫn chuỗi trên một dòng**.
  Bộ test chỉ có dữ liệu mỗi dòng một giá trị thì luôn xanh.
- *Đổi gì trong đầu:* `Scanner` có **ba** quy ước xử lý dòng khác nhau (`nextLine`, `next`,
  `nextInt`), và chúng không tương thích. Chọn một quy ước ngay từ đầu rồi viết hàm đọc
  riêng — đừng trộn.

---

## ⚠️ Anti-messages

| The trap | What actually happens | Repro |
|----------|----------------------|-------|
| "Tràn số sẽ được cảnh báo" | `int` quay vòng im lặng, `-1294967296` là kết quả hợp lệ về mặt ngôn ngữ | `Ch02Narrowing` |
| "Dùng `double` cho mọi thứ an toàn hơn" | `float` mất chính xác ở 2²⁴, `int` thì không | `Ch02Narrowing` |
| "Ép kiểu `(int)` thì Java sẽ cảnh báo nếu mất dữ liệu" | `(int) 1e20 = 2147483647`, bão hòa, không cảnh báo | `Ch02Narrowing` |
| "`byte` + `int` sẽ tự động làm `byte`" | Nó **làm**, và đó chính là chỗ chết người: `byte 127` + 1 = `-128` | `Ch02Narrowing` |
| "Đọc `12,5` thì chắc chắn được" | Locale `US` → `InputMismatchException`; locale `DE` → `12,5` | `Ch02ScannerTraps` |
| "nextLine() sẽ đọc phần còn lại của dòng" | Nó đọc **rỗng** nếu dòng vừa bị `nextInt()` dừng trước | `Ch02ScannerTraps` |

---

## 🆕 Java 21 delta vs sách

| Book | Java 21 | Why it matters here |
|------|---------|--------------------|
| `int radius = 5;` (biến không khởi tạo thì lỗi) | Như cũ — Java **không** có biến chưa khởi tạo | `final` bắt buộc khởi tạo tại khai báo |
| Nhấn mạnh `double` cho số thực | `var` làm ngắn hơn nhưng `var x = 0;` ra `int`, `var x = 0.0;` ra `double` — dễ nhầm | Dùng `var` cùng chương trình nhưng hai kiểu khác nhau |
| `int` là 32 bit | Như cũ, nhưng thêm `Math.toIntExact(long)` và `Math.multiplyExact` | Chuyển `long` → `int` **có kiểm tra**, thay vì âm thầm cắt |
| `Integer`/`Double` như lớp bọc | `new Integer(...)` **deprecated for removal**; dùng `valueOf` | Xem Ch 10 |
| `(double) 2^53+1` mất chính xác | **Không thay đổi.** Đo trên Java 21 với `--release 8`: cùng output `0.30000000000000004`, cùng `== 0.3` là `false` | Phép `double` vẫn sai. Nếu cần chính xác, dùng `BigDecimal` (Ch 10) — **không** phải dựa vào một bản "sửa" của ngôn ngữ |
| `Scanner` là cách đọc chuẩn | `Scanner` vẫn tồn tại nhưng chậm; `BufferedReader` + parse, hoặc `FastScanner` tự viết | Đo ở toolchain: 52× |
| Không có `Locale` trong bài | Giữ nguyên nhưng phải nói rõ: `useLocale(Locale.US)` là **bắt buộc** trên judge | M4-01 |

---

## 🔗 Cầu nối

- **Previous:** [`ch01-computers-programs-java/`](../ch01-computers-programs-java/) — M2-01
  ở đó là phần khởi động, ở đây là toàn bộ.
- **Next:** [`ch03-conditional-statements/`](../ch03-conditional-statements/) — M2 sinh ra
  một câu hỏi mà Ch 3 trả lời: *nếu không có kiểu nào chứa `null` thì trạng thái "chưa có
  giá trị" biểu diễn bằng gì?*
- **To toolchain:** [`00-toolchain/lab/FastScanner.java`](../../../00-toolchain/lab/FastScanner.java)
  — câu trả lời cho M4-01 và M1-03: parse theo byte, không theo locale.
- **To Ch 10:** wrapper classes, autoboxing, `BigInteger` — nơi M2-01 được xử lý triệt để.
- **Diary note:** [02-java-for-dsa.md](https://github.com/NguyenPhan161206/reaserching-diary/blob/main/dev_foundation/dsa/02-java-for-dsa.md)
  §7 viết về tràn số — card này là bằng chứng đo cho mục đó.
- **Lab:** `lab/Ch02ScannerTraps.java`, `lab/Ch02Narrowing.java`.

---

## 🧪 Kết quả lab (output thật)

```
--- trap 1: nextInt() then nextLine() ---
age=25 weight=67.5 height=4.5
nextLine() returned []  (length 0)
  -> the rest of the line is EMPTY; the user's answer is lost.
  -> the correct calls are nextLine() TWICE: [hello world]

--- trap 2: the decimal mark follows the default locale ---
default            locale=US      nextDouble("12,5") -> InputMismatchException
forced to Germany  locale=DE      nextDouble("12,5") = 12,5
```

```
--- widening: implicit, always exact ---
byte 100 -> int 100 -> long 100 -> double 100.0
  widening from int to double is exact only up to 2^53
  2^53+1 as long then as double: 9007199254740993 -> 9007199254740992  (lost!)

--- narrowing: explicit, and lossy ---
(int) 1e+20 = 2147483647

--- `b += 1` compiles, `b = b + 1` does not ---
byte b = 127; b += 1;  ->  b == -128

--- overflow wraps, it does not throw ---
Integer.MAX_VALUE          = 2147483647
Integer.MAX_VALUE + 1      = -2147483648
Integer.MIN_VALUE - 1      = 2147483647
1_000_000_000 * 3 (int)    = -1294967296
1_000_000_000L * 3 (long)   = 3000000000

--- float loses precision where int does not ---
(float) 16_777_217   = 16777216
(double) 16_777_217  = 16777217
```

---

## ❌ What I Got Wrong

- **Tôi viết vào bảng delta rằng Java 9+ đã cải thiện `double` → `String`.** Đo thật:
  `java 21` in `0.30000000000000004`; `java --release 8` in **y hệt**. Tôi đã ghi một thay
  đổi vào bảng dựa trên **ký ức** về một JEP nào đó, không kiểm tra. Bảng delta viết bằng
  trí nhớ là một bảng delta sai — và nó nguy hiểm hơn việc không có bảng, vì nó khiến người
  đọc tin rằng vấn đề đã được sửa. **Quy tắc:** mỗi dòng trong bảng delta phải kèm lệnh
  đã chạy, hoặc phải ghi "chưa xác minh".
- **Lab locale ban đầu của tôi crash chương trình.** Tôi gọi `scanner.nextDouble()` trực
  tiếp trên input `"12,5"` với locale mặc định → `InputMismatchException` làm hỏng cả
  `main`. Tôi chỉ biết điều đó sau khi chạy.
  **Bài học về quy trình:** bài demo về xử lý lỗi mà tự để lỗi chết thì vô dụng. Tôi đã
  sửa để `report()` bắt exception — và *bản sửa dạy nhiều hơn bản gốc*, vì giờ đọc ra cả
  hai nhánh (thành công và thất bại) chứ không chỉ một.
- **`reRead()` trong bản đầu là một hàm bịa.** Tôi viết nó để "in ra giá trị đúng" mà
  không thực sự đọc lại input — tức là in ra một thứ không đo được. Đã xoá và thay bằng
  `report()` thật. **Đây là dạng lỗi tệ nhất trong ghi chú học tập: dàn dựng bằng chứng.**
- **Tôi viết "JVM lưu boolean là 1 bit" ở Ch 01 rồi phải sửa.** Sai ngay trong lúc làm
  Ch 02. Xem mục `What I Got Wrong` của Ch 01.

---

## 🤔 Câu hỏi mở

- [ ] `Math.multiplyExact(1_000_000_000, 3)` ném `ArithmeticException` thay vì tràn. Đổi
      tất cả phép nhân trong một bài DSA từ `int` sang `multiplyExact` — có chậm không, và
      bao nhiêu? (đo được thì tốt hơn đoán)
- [x] ~~`String.valueOf(0.1+0.2)` ở Java 8 vs Java 21?~~ → **đã đo: giống hệt nhau**,
      `0.30000000000000004` cả hai. Xem mục `What I Got Wrong`.
- [ ] Vì sao `FastScanner` không bị ảnh hưởng bởi locale? Nó có `useLocale` không? Nếu
      không thì nó đang *hy sinh* tính năng gì để đổi lấy tốc độ?
