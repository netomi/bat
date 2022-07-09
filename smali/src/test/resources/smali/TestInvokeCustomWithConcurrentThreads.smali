.class public LTestInvokeCustomWithConcurrentThreads;
.super LTestBase;
.source "TestInvokeCustomWithConcurrentThreads.java"

# interfaces
.implements Ljava/lang/Runnable;


# static fields
.field private static final NUMBER_OF_THREADS:I = 0x10

.field private static final barrier:Ljava/util/concurrent/CyclicBarrier;

.field private static final called:[Ljava/util/concurrent/atomic/AtomicInteger;

.field private static final instantiated:[Ljava/lang/invoke/CallSite;

.field private static final nextIndex:Ljava/util/concurrent/atomic/AtomicInteger;

.field private static final targetted:[Ljava/util/concurrent/atomic/AtomicInteger;

.field private static final threadIndex:Ljava/lang/ThreadLocal;
    .annotation system Ldalvik/annotation/Signature;
        value = {
            "Ljava/lang/ThreadLocal<",
            "Ljava/lang/Integer;",
            ">;"
        }
    .end annotation
.end field
