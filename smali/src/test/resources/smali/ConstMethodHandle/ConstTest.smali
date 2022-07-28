.class public Lconstmethodhandle/ConstTest;
.super Ljava/lang/Object;
.source "ConstTest.java"


# direct methods
.method public constructor <init>()V
    .registers 1

    .prologue
    .line 22
    invoke-direct {p0}, Ljava/lang/Object;-><init>()V

    return-void
.end method

.method private static displayMethodHandle(Ljava/lang/invoke/MethodHandle;)V
    .registers 4
    .annotation system Ldalvik/annotation/Throws;
        value = {
            Ljava/lang/Throwable;
        }
    .end annotation

    .prologue
    .line 24
    sget-object v0, Ljava/lang/System;->out:Ljava/io/PrintStream;

    new-instance v1, Ljava/lang/StringBuilder;

    invoke-direct {v1}, Ljava/lang/StringBuilder;-><init>()V

    const-string v2, "MethodHandle "

    invoke-virtual {v1, v2}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    move-result-object v1

    invoke-virtual {v1, p0}, Ljava/lang/StringBuilder;->append(Ljava/lang/Object;)Ljava/lang/StringBuilder;

    move-result-object v1

    const-string v2, " => "

    invoke-virtual {v1, v2}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    move-result-object v1

    const v2, 0x46403000    # 12300.0f

    .line 25
    invoke-static {v2}, Ljava/lang/Float;->valueOf(F)Ljava/lang/Float;

    move-result-object v2

    invoke-polymorphic {p0, v2}, Ljava/lang/invoke/MethodHandle;->invoke([Ljava/lang/Object;)Ljava/lang/Object;, (Ljava/lang/Object;)Ljava/lang/Class;

    move-result-object v2

    invoke-virtual {v1, v2}, Ljava/lang/StringBuilder;->append(Ljava/lang/Object;)Ljava/lang/StringBuilder;

    move-result-object v1

    invoke-virtual {v1}, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;

    move-result-object v1

    .line 24
    invoke-virtual {v0, v1}, Ljava/io/PrintStream;->println(Ljava/lang/String;)V

    .line 26
    return-void
.end method

.method private static displayMethodType(Ljava/lang/invoke/MethodType;)V
    .registers 4

    .prologue
    .line 29
    sget-object v0, Ljava/lang/System;->out:Ljava/io/PrintStream;

    new-instance v1, Ljava/lang/StringBuilder;

    invoke-direct {v1}, Ljava/lang/StringBuilder;-><init>()V

    const-string v2, "MethodType "

    invoke-virtual {v1, v2}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    move-result-object v1

    invoke-virtual {v1, p0}, Ljava/lang/StringBuilder;->append(Ljava/lang/Object;)Ljava/lang/StringBuilder;

    move-result-object v1

    invoke-virtual {v1}, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;

    move-result-object v1

    invoke-virtual {v0, v1}, Ljava/io/PrintStream;->println(Ljava/lang/String;)V

    .line 30
    return-void
.end method

.method public static main([Ljava/lang/String;)V
    .registers 2

    invoke-static {}, Lconstmethodhandle/ConstTest;->test1()Ljava/lang/invoke/MethodHandle;

    move-result-object v0

    invoke-static {v0}, Lconstmethodhandle/ConstTest;->displayMethodHandle(Ljava/lang/invoke/MethodHandle;)V

    invoke-static {}, Lconstmethodhandle/ConstTest;->test2()Ljava/lang/invoke/MethodType;

    move-result-object v0

    invoke-static {v0}, Lconstmethodhandle/ConstTest;->displayMethodType(Ljava/lang/invoke/MethodType;)V

    return-void
.end method

.method public static test1()Ljava/lang/invoke/MethodHandle;
    .registers 1

    const-method-handle v0, invoke-instance@Ljava/lang/Object;->getClass()Ljava/lang/Class;

    return-object v0
.end method

.method public static test2()Ljava/lang/invoke/MethodType;
    .registers 1

    const-method-type v0, (CSIJFDLjava/lang/Object;)Z

    return-object v0
.end method
