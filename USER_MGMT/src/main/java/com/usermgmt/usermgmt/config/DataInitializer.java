package com.usermgmt.usermgmt.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.usermgmt.usermgmt.entity.Role;
import com.usermgmt.usermgmt.repository.RoleRepository;

@Configuration
public class DataInitializer {

    @Bean
    CommandLineRunner initRoles(RoleRepository roleRepo){
        return args -> {
            if (roleRepo.findByName("USER").isEmpty()) roleRepo.save(new Role("USER"));
            if (roleRepo.findByName("ADMIN").isEmpty()) roleRepo.save(new Role("ADMIN"));
        };
    }
}

/*
 Great question — **ye code kab aur kaise execute hota hai?**
Isko step-by-step, *zero confusion* way me samajh lo.

---

# ✅ **1. What is CommandLineRunner?**

`CommandLineRunner` ek Spring Boot ka special interface hai.

➡️ **Application start hote hi, just after Spring Boot initialization**,
isko automatically execute kar diya jata hai.

Yani:

* Spring Boot start hua
* Beans create hue
* ApplicationContext ready hua
* **Uske turant baad CommandLineRunner.run() automatically chalega**

---

# ✅ **2. Yeh Method Kab Execute Hota Hai?**

### 🔥 **Jab aap application RUN karte ho:**

* `mvn spring-boot:run`
* or run from STS/IntelliJ
* or run JAR: `java -jar app.jar`
* or run inside Docker

Every time the application starts → **`initRoles()` code chal jayega**

---

# ⚙️ **3. How does it get executed?**

### Step-by-step:

### ✔️ Step 1: Spring sees @Configuration class

`DataInitializer` class start me scan hogi.

### ✔️ Step 2: Spring sees @Bean method

`@Bean CommandLineRunner initRoles(...)` define karta hai ek bean.

### ✔️ Step 3: Spring Boot application startup sequence

Beans create hone ke baad Spring Boot **CommandLineRunner** beans ko detect karta hai.

### ✔️ Step 4: It automatically runs the lambda

Yeh code chalega:

```java
if (roleRepo.findByName("USER").isEmpty()) 
    roleRepo.save(new Role("USER"));

if (roleRepo.findByName("ADMIN").isEmpty()) 
    roleRepo.save(new Role("ADMIN"));
```

---

# 🎯 **4. Why do we use CommandLineRunner here?**

Because:

✔️ Ye ensure karta hai ki **roles table hamesha populated ho**
✔️ First-time startup par **USER & ADMIN roles create ho jayen**
✔️ Database me manually role insert nahi karna padta

---

# 📌 **5. Important Point**

CommandLineRunner:

* controller nahi chalata
* API call nahi hota
* koi manual trigger nahi chahiye

**It runs automatically during startup.**

---

# 🧠 **6. Example Flow**

Imagine aap app start karte ho first time:

### Startup Flow:

1. Spring Boot start
2. Database connection create
3. Repositories initialize
4. `DataInitializer` class loaded
5. `initRoles()` bean create
6. **CommandLineRunner executes**
7. USER inserted
8. ADMIN inserted
9. Application fully started

If you restart app again:

* Roles already exist → code checks → **duplicate nahi banega**

---

# 🏆 One-line answer (super simple):

**CommandLineRunner automatically runs AFTER Spring Boot starts and BEFORE the application begins serving requests. It inserts roles in DB only at startup.**

---

Agar chaho to main tumhare project ka **complete startup lifecycle diagram** bhi bana dunga.

 */