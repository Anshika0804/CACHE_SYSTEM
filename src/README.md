🔐 What is ReentrantLock?
ReentrantLock is a class in java.util.concurrent.locks package.
It is a mutual exclusion lock — meaning only one thread can hold the lock at a time.
It’s called "reentrant" because a thread can acquire the same lock multiple times, and it won’t deadlock itself. The thread just has to release it the same number of times it acquired it.


🔧 What is scheduleAtFixedRate(...)?
    It’s a method from Java’s ScheduledExecutorService that:
    👉 Runs a task repeatedly after a fixed time between the start of one run and the start of the next.

📌 Parameters explained:
() -> { ... }
    This is a lambda expression — basically, it’s the code block that should run repeatedly.
    
    In your case, this code finds and removes expired cache entries.
    
    1 (Initial delay)
    This means: wait 1 second before running the task for the first time.
    
    It avoids running the cleaner immediately when the app starts.
    
    1 (Period)
    This means: run the cleaner task every 1 second.
    
    More specifically: once a run starts, the next one starts 1 second later, regardless of whether the previous one is done or not (unless it’s still running — in which case it waits).
    
    TimeUnit.SECONDS
    This defines the unit of time for the previous 2 numbers (1 and 1).




