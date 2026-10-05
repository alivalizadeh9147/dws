```text
با سلام و وقت بخیر
زمان انجام تسک بنظرم خیلی کوتاه بود
این کلا نتیجه نوشتن کد تو زمان 7 ساعت هست

ایده ای که خودم دوست داشتم انجام بدم این بود که api-gateway داشته باشم که authentication درخواست هارو انجام بده، بعدش route کنه به سرویس مورد نظر (wallet, user, transaction)
دوست داشتم که sso پیاده سازی بشه تا به واسطه jwks بشه تمامی سرویس ها authentication مرکزی داشته باشن
سپس به واسه یک service-discovery که با eureka مد نظرم بود بیام مدیریت و ارتباط بین سرویس هارو انجام بدم که متاسفانه وقت نشد انجام داده بشه

در حال حاضر سرویس wallet به صورت correctness, concurrent هست و همچنین موجودی هیچگاه منفی نخواهد شد

سرویس wallet:
از معماری hexagonal با approach DDD پیاده سازی شده تا infrastructure-independent باشه.
عملیات های credit, debit, transfer, openWallet پیاده سازی شده و طی هر عملیات event هایی publish می شوند.
این publish به واسطه outbox صورت میگیره
سپس scheduler از outbox سطر دیتابیس مد نظر که ارسال نشده رو دریافت میکنه و به mq ارسال میکنه



سرویس transaction:
سرویس transaction تمامی رویداد هایی که مربوط به wallet هست رو دریافت میکنه و از inbox pattern استفاده شده
متاسفانه فرصت نشد تا ذخیره دیتاهارو به صورت سازماندهی شده تو دیتابیس داشته باشم تا بتونم به عنوان لیست تراکنش ها اونو برگردونم

سرویس user:
مدیریت اصلی user ها هست که دو سرویس register, login داره و همچنین سرویس jwks رو هم داره تا بقیه سرویس ها بواسطه اون بتونن token رو validate کنن
```

```text
Run configuration :

1) run command :
docker network create shared-network

2) in root :
docker compose up -d
در این مرحله دیتابیس، rabbitmq و redis به صورت داکرایز بالا خواهند امد

3) mvn clean package -Drevision=1.0.0
در این مرحله تمامی unit tests, integration test اجرا خواهند شد و سپس jar فایل ساخته خواهد شد

4) cd wallet-service
5) cd container
6) docker compose up -d

7) cd ../..
8) cd transaction-service
9) docker compose up -d

10) cd ..
11) cd user-service
12) docker-compose up -d

سپس به این صورت اجرا می شود:
user-service: port -> 8082
transaction-service: port -> 8083
wallet-service: port -> 8081

سرویس ها دارای swagger می باشند
http://localhost:8082/swagger-ui/index.html
http://localhost:8081/swagger-ui/index.html
```

```text
پروژه در حال تستی خودم تست های زیر رو داره :
idempotency-test.js
load-test.js
transfer-load-test.js

این اسپریت ها برای تست سرویس های wallet به صورت concurrent هست تا وضعیت idempotency- correctness رو بررسی و validate کنن
اما این تست ها قابلیت تست مجزا ندارن که چند مرحله ای بخواد تست بشه تا از مرحله ساخت wallet تا transfer رو تست بکنه
این تست ها در زمان تست خودم نوشته شده است که با مکانیزم توکن در حال حاضر نیاز به تغییر دارد

برای tracing, observability علاقه داشتم تا میتونستم open telemetry هم استفاده کنم و از kibana جهت نمایش استفاده کنم اما امکانش نبود
همچنین برای migration database هم دوست داشتم از liquibase استفاده کنم که فرصت نشد.

امیدوارم تا همینجا قابل قبول باشه و مورد قبول باشه.
```