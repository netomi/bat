.class public LHelloWorld;
.super Ljava/lang/Object;

.field public static A:I = 0x20

.method public constructor <init>()V
    .registers 1
    invoke-direct {p0}, Ljava/lang/Object;-><init>()V
    return-void
.end method

.method public static main2([Ljava/lang/String;)V
    .registers 3

    sget-object v0, Ljava/lang/System;->out:Ljava/io/PrintStream;

    new-instance v1, LHelloWorld;
    invoke-direct {v1}, LHelloWorld;-><init>()V
    invoke-virtual {v1}, LHelloWorld;->virtualMethod()Ljava/lang/String;
    move-result-object v1

    invoke-virtual {v0, v1}, Ljava/io/PrintStream;->println(Ljava/lang/Object;)V

    return-void
.end method


.method public virtualMethod()Ljava/lang/String;
    .registers 1

    const-string v0, "in virtualMethod()"
    return-object v0
.end method


.method public static main([Ljava/lang/String;)V
    .registers 2

    sget-object v0, Ljava/lang/System;->out:Ljava/io/PrintStream;

    const-string	v1, "Hello World!"

    invoke-virtual {v0, v1}, Ljava/io/PrintStream;->println(Ljava/lang/String;)V

    sget v1, LHelloWorld;->A:I

    invoke-virtual {v0, v1}, Ljava/io/PrintStream;->println(I)V

    const/4 v1, 0x2

    sput v1, LHelloWorld;->A:I

    const/4 v1, 0x5

    sget v1, LHelloWorld;->A:I

    invoke-virtual {v0, v1}, Ljava/io/PrintStream;->println(I)V

    return-void
.end method
