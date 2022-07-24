.class public LExceptionTest;
.super Ljava/lang/Object;
.source "ExceptionTest.smali"

.method public constructor <init>()V
    .registers 1
    invoke-direct {p0}, Ljava/lang/Object;-><init>()V
    return-void
.end method

.method public singleCatchTest()V
    .registers 2
    .annotation runtime Lorg/junit/Test;
    .end annotation

    :try_start
    new-instance v0, Ljava/lang/Exception;
    const-string v1, "This is an error message"
    invoke-direct {v0, v1}, Ljava/lang/Exception;-><init>(Ljava/lang/String;)V
    throw v0
    :try_end
    .catch Ljava/lang/Exception; {:try_start .. :try_end} :handler

    :handler
    #no need to test anything. If it didn't catch the exception, the test would fail
	return-void
.end method

.method public nestedCatchTest()V
    .registers 2
    .annotation runtime Lorg/junit/Test;
    .end annotation

    :try_start_outer
    nop
    nop

    :try_start_inner
    new-instance v0, Ljava/lang/RuntimeException;
    const-string v1, "This is an error message"
    invoke-direct {v0, v1}, Ljava/lang/RuntimeException;-><init>(Ljava/lang/String;)V
    throw v0
    :try_end_inner
    .catch Ljava/lang/RuntimeException; {:try_start_inner .. :try_end_outer} :handler_inner

    nop
    nop
    :try_end_outer

    .catch Ljava/lang/Exception; {:try_start_outer .. :try_end_outer} :handler_outer

    :handler_outer
    invoke-static {}, Lorg/junit/Assert;->fail()V

    :handler_inner

	return-void
.end method

.method public catchAllTest()V
    .registers 2
    .annotation runtime Lorg/junit/Test;
    .end annotation

    :try_start
    new-instance v0, Ljava/lang/Exception;
    const-string v1, "This is an error message"
    invoke-direct {v0, v1}, Ljava/lang/Exception;-><init>(Ljava/lang/String;)V
    throw v0
    :try_end
    .catchall {:try_start .. :try_end} :handler

    :handler
    #no need to test anything. If it didn't catch the exception, the test would fail
	return-void
.end method

.method public nestedCatchAllTest()V
    .registers 2
    .annotation runtime Lorg/junit/Test;
    .end annotation

    :try_start_outer
    nop
    nop

    :try_start_inner
    new-instance v0, Ljava/lang/RuntimeException;
    const-string v1, "This is an error message"
    invoke-direct {v0, v1}, Ljava/lang/RuntimeException;-><init>(Ljava/lang/String;)V
    throw v0
    :try_end_inner
    .catch Ljava/lang/RuntimeException; {:try_start_inner .. :try_end_outer} :handler_inner

    nop
    nop
    :try_end_outer

    .catchall {:try_start_outer .. :try_end_outer} :handler_outer

    :handler_outer
    invoke-static {}, Lorg/junit/Assert;->fail()V

    :handler_inner


	return-void
.end method

.method public testCatchAndCatchAllTogether()V
    .registers 2
    .annotation runtime Lorg/junit/Test;
    .end annotation

    const v0, 5

    :try_start1
    goto :label1
    nop
    nop
    nop
    nop
    nop
    nop
    nop
    nop
    nop
    nop
    :try_start2
    nop
    nop
    :try_end2
    .catch Ljava/lang/RuntimeException; {:try_start2 .. :try_end2} :handler2
    .catchall {:try_start2 .. :try_end2} :handler3
    nop
    nop
    nop
    nop
    new-instance v0, Ljava/lang/Exception;
    const-string v1, "This is an error message"
    invoke-direct {v0, v1}, Ljava/lang/Exception;-><init>(Ljava/lang/String;)V
    throw v0
    :try_end1
    nop
    nop
    nop
    nop
    nop
    nop
    .catch Ljava/lang/Exception; {:try_start1 .. :try_end1} :handler1
    :handler1
    nop
    nop
    nop
    nop
    nop
    nop
    :handler2
    nop
    nop
    nop
    nop
    nop
    nop
    nop
    :handler3
    nop
    nop
    nop
    invoke-static {}, Lorg/junit/Assert;->fail()V


    :label2

    const v1, 6
    invoke-static {v0, v1}, LAssert;->assertEquals(II)V
    return-void

    invoke-static {}, Lorg/junit/Assert;->fail()V
    nop
    nop
    invoke-static {}, Lorg/junit/Assert;->fail()V


    :label1
    const v0, 6
    goto :label2


    invoke-static {}, Lorg/junit/Assert;->fail()V
    return-void
.end method