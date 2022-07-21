.class public LArrayTests;
.super Ljava/lang/Object;

.method public constructor <init>()V
    .registers 1
    invoke-direct {p0}, Ljava/lang/Object;-><init>()V
    return-void
.end method

.method public test_filled-new-array()V
    .registers 3
    .annotation runtime Lorg/junit/Test;
    .end annotation

    const v0, 10
    const v1, 20
    const v2, 30

    filled-new-array {v0, v1, v2}, [I
    move-result-object v0

    const v1, 0
    aget v2, v0, v1
    const v1, 10
    invoke-static {v1, v2}, LAssert;->assertEquals(II)V

    const v1, 1
    aget v2, v0, v1
    const v1, 20
    invoke-static {v1, v2}, LAssert;->assertEquals(II)V

    const v1, 2
    aget v2, v0, v1
    const v1, 30
    invoke-static {v1, v2}, LAssert;->assertEquals(II)V

    return-void
.end method

.method public test_filled-new-array-range()V
    .registers 6
    .annotation runtime Lorg/junit/Test;
    .end annotation

    const v0, 1
    const v1, 2
    const v2, 3
    const v3, 4
    const v4, 5
    const v5, 6


    filled-new-array/range {v0 .. v5}, [I
    move-result-object v0

    const v1, 0
    aget v2, v0, v1
    const v1, 1
    invoke-static {v1, v2}, LAssert;->assertEquals(II)V

    const v1, 1
    aget v2, v0, v1
    const v1, 2
    invoke-static {v1, v2}, LAssert;->assertEquals(II)V

    const v1, 2
    aget v2, v0, v1
    const v1, 3
    invoke-static {v1, v2}, LAssert;->assertEquals(II)V

    const v1, 3
    aget v2, v0, v1
    const v1, 4
    invoke-static {v1, v2}, LAssert;->assertEquals(II)V

    const v1, 4
    aget v2, v0, v1
    const v1, 5
    invoke-static {v1, v2}, LAssert;->assertEquals(II)V

    const v1, 5
    aget v2, v0, v1
    const v1, 6
    invoke-static {v1, v2}, LAssert;->assertEquals(II)V

    return-void
.end method
