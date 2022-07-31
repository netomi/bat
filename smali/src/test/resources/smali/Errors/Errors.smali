.class public LMain;
.super Ljava/lang/Object;

.method public static main([Ljava/lang/String;)V
    .registers 2

    const v0, 6
    new-array v0, v0, [I
    fill-array-data v0, :arrayData1

    sget-object v0, Ljava/lang/System;->out:Ljava/io/PrintStream;
    sget-object v1, LEnum;->VALUE1:LEnum;

    invoke-virtual {v0, v1}, Ljava/io/PrintStream;->println(Ljava/lang/Object;)V

	return-void

    :arrayData
    .array-data 4
        1 2 3 4 5 6
    .end array-data
.end method