# Ch01 — Exercises

**Book ref:** Ch.1 · **Messages:** M1, M6
**Rule:** answers are at the bottom. Do not scroll until you have written your own answer.

---

## L1 — Recall (1 bài, 10′)

**Bài 1.1.** Viết bằng một câu: `javac` làm gì, `java` làm gì, và vì sao cần cả hai?

---

## L2 — Apply (2 bài, ≤60′)

**Bài 2.1 — Ước lượng rồi kiểm chứng.** Một vòng lặp cộng `int` chạy 20.000.000 phép.
Ước lượng (không chạy) thời gian của lần chạy đầu tiên và của lần chạy thứ mười, nếu
bạn biết máy chạy 3,5 GHz và một phép `iadd` mất 1 chu kỳ, một lần đọc/ghi biến cục bộ mất
1 chu kỳ, còn bytecode được **diễn giải** với khoảng 30 lệnh máy cho mỗi `iadd` thay vì 1.

Sau đó chạy `lab/Ch01JitWarmup.java` và so tỉ lệ với ước lượng của bạn.

**Bài 2.2 — Quyết định kiểu, giải thích bằng byte.** Chọn kiểu cho mỗi biến, nêu lý do
dựa trên kích thước và phạm vi (không dựa vào "quy ước"):

| Biến | Giá trị | Kiểu bạn chọn | Bytes | Phạm vi có đủ không? |
|------|---------|---------------|-------|---------------------|
| `a` | tuổi của một người | | | |
| `b` | số dân một tỉnh (3 chữ số) | | | |
| `c` | tiền lương tính bằng xu (số nguyên) | | | |
| `d` | tỉ số trận đấu bóng đá, ví dụ 2.5 | | | |

---

## L3 — Derive (1 bài, phải **đo**)

**Bài 3.1 — Boxing: bộ nhớ hay thời gian?**

Viết một chương trình so sánh, trên cùng một mảng dữ liệu và cùng một phép toán:

1. `int[]` cộng vào một `long` cục bộ
2. `Integer[]` cộng vào một `long` cục bộ
3. `List<Long>` cộng bằng `add()` cho từng phần tử

Đo **thời gian** và **bộ nhớ đã cấp phát thực tế** (dùng
`com.sun.management.ThreadMXBean#getThreadAllocatedBytes(long)` — đừng suy ra, hãy đo).

Sau đó trả lời bằng văn bản:

- Bước nào chậm nhất? Vì sao?
- Bước nào dùng nhiều bộ nhớ nhất? Vì sao?
- Kết luận của bạn có khớp với câu "boxing là khoản thuế ẩn" không? Nếu không khớp thì
  câu đó cần sửa thành gì?

> Bắt buộc dán lệnh đo, output thật, và `System.gc()` trước mỗi lần đo.

---

## 🧠 5 câu tự kiểm (nhắm mắt, không mở tài liệu)

1. Vì sao `-Xint` làm chậm ~30× với một file `.class` **không đổi một byte nào**?
2. `Integer.valueOf(1000) == Integer.valueOf(1000)` cho `false` còn `Integer.valueOf(1) ==
   Integer.valueOf(1)` cho `true`. Điều gì trong JDK đảm bảo điều đó, và điều đó có phải
   là một phần của đặc tả ngôn ngữ không?
3. Ba tầng dịch từ `.java` đến chu kỳ CPU là gì? Tầng nào quyết định tốc độ batch 1 và
   tầng nào quyết định tốc độ batch 10?
4. `Boolean.BYTES` không tồn tại. JLS nói gì về kích thước boolean, và vì sao câu trả lời
   đó không áp dụng cho một trường boolean của object?
5. Nếu một bài kiểm thử của bạn dùng `assertEquals(999, box(999))` với `Integer` và nó
   **xanh**, bạn vừa học được điều gì về độ tin cậy của bài kiểm thử đó?

---

## ✅ Đáp án gợi ý

<details>
<summary>L1</summary>

`javac` biên dịch mã nguồn thành **bytecode** trong file `.class` (ngôn ngữ trung gian,
một dạng assembly của JVM). `java` là JVM: nạp `.class`, kiểm tra bytecode, rồi thực
thi — ban đầu bằng bộ thông dịch, sau đó bằng mã máy do JIT sinh ra. Cần cả hai vì
`.class` không chạy được trên bất kỳ CPU nào và `.java` không phải thứ CPU hiểu.
</details>

<details>
<summary>L2</summary>

**2.1** Cách tính (một cách, không phải cách duy nhất):

- Lần chạy đầu (diễn giải): $2\times10^7$ phép × ~30 lệnh máy/lệnh-máy × 1 chu kỳ ÷
  3,5 GHz ≈ **170 ms** nếu interpreter không tối ưu gì. Thực tế `Ch01JitWarmup` đo
  **5,6 ms** cho 2 triệu phép → **56 ms** cho 20 triệu. Số đo nhỏ hơn ước lượng ~3×
  vì bộ thông dịch có fast-path cho các mẫu byte mã phổ biến.
- Lần thứ mười (JIT): ~1 lệnh máy cho `iadd` + 1 cho nạp + 1 cho lưu ≈ 3 chu kỳ →
  $2\times10^7 \times 3 / 3{,}5\times10^9$ ≈ **17 ms**. Đo được: batch 5–10 chạy
  0,26 ns/op → 20 triệu phép ≈ **5 ms**.

Tỉ lệ đo được 10,6×; ước lượng 56/5 ≈ 11×. **Ước lượng của bạn chỉ cần đúng thứ tự
độ lớn** — mục đích là hiểu vì sao đo ở batch 1 là sai.

**2.2**

| Biến | Kiểu | Bytes | Lý do |
|------|------|-------|-------|
| `a` tuổi | `int` | 4 | 0–150; dùng `byte` (-128..127) là đủ nhưng ép kiểu vô ích khi in |
| `b` số dân | `int` | 4 | tới 999.999.999; `short` **không đủ** (max 32.767) — đây là bẫy |
| `c` lương xu | `long` | 8 | tỷ lệ 100–200 USD/giờ × 40 giờ × 52 tuần ≈ 4×10⁷ xu/năm, nhân vài năm sẽ vượt `int.max` |
| `d` tỉ số | `double` | 8 | cần phân số (2.5); `float` có ~7 chữ số, đủ ở đây nhưng `double` là mặc định an toàn |

Bài học: `b` cho thấy **"số nhỏ" không đồng nghĩa "kiểu nhỏ"** — phải so sánh với
`Short.MAX_VALUE`, không so sánh với cảm nhận.
</details>

<details>
<summary>L3</summary>

Kết quả phụ thuộc máy, nhưng **thứ tự** ổn định trên mọi máy:

1. `int[]` — nhanh nhất về thời gian, 4 byte/phần tử.
2. `Integer[]` — thời gian gần bằng `int[]` (JIT unbox trong vòng lặp, tham chiếu
   `Integer` cũng liên tiếp trong bộ nhớ), nhưng 16 byte/phần tử.
3. `List<Long>` — chậm nhất, vì `add()` biến **kết quả** mỗi phép thành một `Long` mới,
   và backing array của `ArrayList` phải `grow` nhiều lần.

Kết luận phải viết lại thành: *boxing trong `Integer[]` là khoản thuế **bộ nhớ**
(~12×), không phải thuế thời gian; thuế thời gian xuất hiện khi boxing xảy ra **trong
vòng lặp** và kết quả phải được lưu lại.*

Phản chứng cho câu "boxing là $O(n)$ ẩn": ở `Integer[]` thời gian gần như bằng `int[]`,
nên thứ tự độ phức tạp **không đổi** — chỉ hằng số và bộ nhớ đổi.
</details>

<details>
<summary>Câu 5 — về độ tin cậy của bài kiểm thử</summary>

Một assertion `assertEquals(999, someInteger)` có thể **vô tình** so sánh tham chiếu
thay vì giá trị (nếu dùng `assertSame`), hoặc xanh chỉ vì cả hai cùng trỏ tới cache. Bài
kiểm thử xanh không bảo chứng điều bạn nghĩ nó kiểm tra. Vì vậy quy tắc của track này:
**test phải từng đỏ ít nhất một lần** (xem `00-toolchain/junit-demo/`, 11/14 failure trước
khi sửa). Test chưa từng đỏ là một giả định, không phải bằng chứng.
</details>
