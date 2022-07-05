.class public Lcom/google/android/test/Test;
.super Landroid/app/Activity;
.source "Test.java"

# interfaces
.implements Ljava/lang/Runnable;


# static fields
.field private static sArray:[I

.field private static sB:B

.field private static sBool:Z

.field private static sC:C

.field private static sD:D

.field private static sF:F

.field private static sI:I

.field private static sL:J

.field private static sO:Ljava/lang/Object;

.field private static sS:S


# instance fields
.field private aBool:[Z

.field private aByte:[B

.field private aChar:[C

.field private aDouble:[D

.field private aFloat:[F

.field private aInt:[I

.field private aLong:[J

.field private aObject:[Ljava/lang/Object;

.field private aShort:[S

.field private mArray:[I

.field private mB:B

.field private mBool:Z

.field private mC:C

.field private mD:D

.field private mF:F

.field private mI:I

.field private mL:J

.field private mO:Ljava/lang/Object;

.field private mRunner:Ljava/lang/Runnable;

.field private mS:S


# direct methods
.method static constructor <clinit>()V
    .registers 2

    .prologue
    .line 7
    const/4 v0, 0x0

    sput-boolean v0, Lcom/google/android/test/Test;->sBool:Z

    .line 8
    const/16 v0, 0x1f

    sput-byte v0, Lcom/google/android/test/Test;->sB:B

    .line 9
    const v0, 0xffff

    sput-char v0, Lcom/google/android/test/Test;->sC:C

    .line 10
    const/16 v0, 0x1234

    sput-short v0, Lcom/google/android/test/Test;->sS:S

    .line 11
    const v0, 0x12345678

    sput v0, Lcom/google/android/test/Test;->sI:I

    .line 12
    const-wide v0, 0x12345679abcdffffL    # 5.626353777594563E-221

    sput-wide v0, Lcom/google/android/test/Test;->sL:J

    .line 13
    const v0, 0x4640e400    # 12345.0f

    sput v0, Lcom/google/android/test/Test;->sF:F

    .line 14
    const-wide v0, 0x40c81c8000000000L    # 12345.0

    sput-wide v0, Lcom/google/android/test/Test;->sD:D

    .line 15
    const/4 v0, 0x0

    sput-object v0, Lcom/google/android/test/Test;->sO:Ljava/lang/Object;

    .line 16
    const/16 v0, 0x8

    new-array v0, v0, [I

    fill-array-data v0, :array_36

    sput-object v0, Lcom/google/android/test/Test;->sArray:[I

    return-void

    nop

    :array_36
    .array-data 4
        0x1
        0x2
        0x3
        0x4
        0x5
        0x6
        0x7
        0x8
    .end array-data
.end method

.method public constructor <init>()V
    .registers 9

    .prologue
    const-wide/16 v6, 0x0

    const/4 v5, 0x1

    const/4 v4, 0x2

    .line 43
    invoke-direct {p0}, Landroid/app/Activity;-><init>()V

    .line 18
    const/4 v1, 0x0

    iput-boolean v1, p0, Lcom/google/android/test/Test;->mBool:Z

    .line 19
    const/16 v1, 0x1f

    iput-byte v1, p0, Lcom/google/android/test/Test;->mB:B

    .line 20
    const v1, 0xffff

    iput-char v1, p0, Lcom/google/android/test/Test;->mC:C

    .line 21
    const/16 v1, 0x1234

    iput-short v1, p0, Lcom/google/android/test/Test;->mS:S

    .line 22
    const v1, 0x12345678

    iput v1, p0, Lcom/google/android/test/Test;->mI:I

    .line 23
    const-wide v2, 0x12345679abcdffffL    # 5.626353777594563E-221

    iput-wide v2, p0, Lcom/google/android/test/Test;->mL:J

    .line 24
    const v1, 0x4640e400    # 12345.0f

    iput v1, p0, Lcom/google/android/test/Test;->mF:F

    .line 25
    const-wide v2, 0x40c81c8000000000L    # 12345.0

    iput-wide v2, p0, Lcom/google/android/test/Test;->mD:D

    .line 26
    const/4 v1, 0x0

    iput-object v1, p0, Lcom/google/android/test/Test;->mO:Ljava/lang/Object;

    .line 27
    const/4 v1, 0x4

    new-array v1, v1, [I

    fill-array-data v1, :array_aa

    iput-object v1, p0, Lcom/google/android/test/Test;->mArray:[I

    .line 31
    new-array v1, v4, [Z

    aput-boolean v5, v1, v5

    iput-object v1, p0, Lcom/google/android/test/Test;->aBool:[Z

    .line 32
    new-array v1, v4, [B

    fill-array-data v1, :array_b6

    iput-object v1, p0, Lcom/google/android/test/Test;->aByte:[B

    .line 33
    new-array v1, v4, [C

    fill-array-data v1, :array_bc

    iput-object v1, p0, Lcom/google/android/test/Test;->aChar:[C

    .line 34
    new-array v1, v4, [S

    iput-object v1, p0, Lcom/google/android/test/Test;->aShort:[S

    .line 35
    new-array v1, v4, [I

    fill-array-data v1, :array_c2

    iput-object v1, p0, Lcom/google/android/test/Test;->aInt:[I

    .line 36
    new-array v1, v4, [J

    fill-array-data v1, :array_ca

    iput-object v1, p0, Lcom/google/android/test/Test;->aLong:[J

    .line 37
    new-array v1, v4, [F

    fill-array-data v1, :array_d6

    iput-object v1, p0, Lcom/google/android/test/Test;->aFloat:[F

    .line 38
    new-array v1, v4, [D

    fill-array-data v1, :array_de

    iput-object v1, p0, Lcom/google/android/test/Test;->aDouble:[D

    .line 39
    new-array v1, v4, [Ljava/lang/Object;

    new-instance v2, Ljava/lang/Object;

    invoke-direct {v2}, Ljava/lang/Object;-><init>()V

    aput-object v2, v1, v5

    iput-object v1, p0, Lcom/google/android/test/Test;->aObject:[Ljava/lang/Object;

    .line 45
    const/4 v1, 0x3

    :try_start_7a
    invoke-direct {p0, v1}, Lcom/google/android/test/Test;->doit(I)V
    :try_end_7d
    .catch Ljava/lang/Exception; {:try_start_7a .. :try_end_7d} :catch_9f
    .catchall {:try_start_7a .. :try_end_7d} :catchall_a6

    .line 49
    iput-wide v6, p0, Lcom/google/android/test/Test;->mL:J

    .line 51
    :goto_7f
    invoke-direct {p0, p0}, Lcom/google/android/test/Test;->add(Ljava/lang/Object;)Ljava/lang/Object;

    move-result-object v1

    iput-object v1, p0, Lcom/google/android/test/Test;->mO:Ljava/lang/Object;

    .line 52
    invoke-static {p0}, Lcom/google/android/test/Test;->adds(Ljava/lang/Object;)Ljava/lang/Object;

    move-result-object v1

    sput-object v1, Lcom/google/android/test/Test;->sO:Ljava/lang/Object;

    .line 53
    invoke-direct {p0}, Lcom/google/android/test/Test;->copies()V

    .line 54
    invoke-direct {p0}, Lcom/google/android/test/Test;->seta()V

    .line 55
    invoke-direct {p0}, Lcom/google/android/test/Test;->geta()Z

    move-result v1

    if-eqz v1, :cond_9e

    .line 56
    sget-object v1, Ljava/lang/System;->out:Ljava/io/PrintStream;

    const-string v2, "ok then"

    invoke-virtual {v1, v2}, Ljava/io/PrintStream;->println(Ljava/lang/String;)V

    .line 57
    :cond_9e
    return-void

    .line 46
    :catch_9f
    move-exception v0

    .line 47
    .local v0, "e":Ljava/lang/Exception;
    const/4 v1, 0x5

    :try_start_a1
    iput v1, p0, Lcom/google/android/test/Test;->mI:I
    :try_end_a3
    .catchall {:try_start_a1 .. :try_end_a3} :catchall_a6

    .line 49
    iput-wide v6, p0, Lcom/google/android/test/Test;->mL:J

    goto :goto_7f

    .line 48
    .end local v0    # "e":Ljava/lang/Exception;
    :catchall_a6
    move-exception v1

    .line 49
    iput-wide v6, p0, Lcom/google/android/test/Test;->mL:J

    .line 50
    throw v1

    .line 27
    :array_aa
    .array-data 4
        0x1
        0x2
        0x3
        0x4
    .end array-data

    .line 32
    :array_b6
    .array-data 1
        0x1t
        0x2t
    .end array-data

    .line 33
    nop

    :array_bc
    .array-data 2
        0x61s
        0x62s
    .end array-data

    .line 35
    :array_c2
    .array-data 4
        0x1
        0x2
    .end array-data

    .line 36
    :array_ca
    .array-data 8
        0x1
        0x2
    .end array-data

    .line 37
    :array_d6
    .array-data 4
        0x3f800000    # 1.0f
        0x40000000    # 2.0f
    .end array-data

    .line 38
    :array_de
    .array-data 8
        0x3ff0000000000000L    # 1.0
        0x4000000000000000L    # 2.0
    .end array-data
.end method

.method private declared-synchronized add(Ljava/lang/Object;)Ljava/lang/Object;
    .registers 13
    .param p1, "o"    # Ljava/lang/Object;

    .prologue
    const/high16 v10, 0x40800000    # 4.0f

    const-wide/high16 v8, 0x4010000000000000L    # 4.0

    .line 179
    monitor-enter p0

    :try_start_5
    iput-object p1, p0, Lcom/google/android/test/Test;->mO:Ljava/lang/Object;

    .line 180
    iget-boolean v0, p0, Lcom/google/android/test/Test;->mBool:Z

    or-int/lit8 v0, v0, 0x0

    iput-boolean v0, p0, Lcom/google/android/test/Test;->mBool:Z

    .line 181
    iget-byte v0, p0, Lcom/google/android/test/Test;->mB:B

    add-int/lit8 v0, v0, 0x1f

    int-to-byte v0, v0

    iput-byte v0, p0, Lcom/google/android/test/Test;->mB:B

    .line 182
    iget-char v0, p0, Lcom/google/android/test/Test;->mC:C

    const v1, 0xffff

    add-int/2addr v0, v1

    int-to-char v0, v0

    iput-char v0, p0, Lcom/google/android/test/Test;->mC:C

    .line 183
    iget-short v0, p0, Lcom/google/android/test/Test;->mS:S

    add-int/lit16 v0, v0, 0x1234

    int-to-short v0, v0

    iput-short v0, p0, Lcom/google/android/test/Test;->mS:S

    .line 184
    iget v0, p0, Lcom/google/android/test/Test;->mI:I

    const v1, 0x12345678

    add-int/2addr v0, v1

    iput v0, p0, Lcom/google/android/test/Test;->mI:I

    .line 185
    iget v0, p0, Lcom/google/android/test/Test;->mI:I

    const/high16 v1, 0x1ff10000

    add-int/2addr v0, v1

    iput v0, p0, Lcom/google/android/test/Test;->mI:I

    .line 186
    iget-wide v0, p0, Lcom/google/android/test/Test;->mL:J

    const-wide v2, 0x12345679abcdffffL    # 5.626353777594563E-221

    add-long/2addr v0, v2

    iput-wide v0, p0, Lcom/google/android/test/Test;->mL:J

    .line 187
    iget-wide v0, p0, Lcom/google/android/test/Test;->mL:J

    const-wide/high16 v2, 0x1ff1000000000000L    # 7.92448702690022E-155

    add-long/2addr v0, v2

    iput-wide v0, p0, Lcom/google/android/test/Test;->mL:J

    .line 188
    iget v0, p0, Lcom/google/android/test/Test;->mF:F

    const v1, 0x4640e400    # 12345.0f

    iget v2, p0, Lcom/google/android/test/Test;->mF:F

    const/high16 v3, 0x3f800000    # 1.0f

    sub-float/2addr v2, v3

    add-float/2addr v1, v2

    iget v2, p0, Lcom/google/android/test/Test;->mF:F

    mul-float/2addr v2, v10

    const/high16 v3, 0x3fc00000    # 1.5f

    div-float/2addr v2, v3

    add-float/2addr v1, v2

    add-float/2addr v0, v1

    iput v0, p0, Lcom/google/android/test/Test;->mF:F

    .line 189
    iget-wide v0, p0, Lcom/google/android/test/Test;->mD:D

    const-wide v2, 0x40c81c8000000000L    # 12345.0

    iget-wide v4, p0, Lcom/google/android/test/Test;->mD:D

    const-wide/high16 v6, 0x3ff0000000000000L    # 1.0

    sub-double/2addr v4, v6

    add-double/2addr v2, v4

    iget-wide v4, p0, Lcom/google/android/test/Test;->mD:D

    mul-double/2addr v4, v8

    const-wide/high16 v6, 0x3ff8000000000000L    # 1.5

    div-double/2addr v4, v6

    add-double/2addr v2, v4

    add-double/2addr v0, v2

    iput-wide v0, p0, Lcom/google/android/test/Test;->mD:D

    .line 190
    iget v0, p0, Lcom/google/android/test/Test;->mF:F

    const/4 v1, 0x0

    cmpl-float v0, v0, v1

    if-eqz v0, :cond_9e

    iget v0, p0, Lcom/google/android/test/Test;->mF:F

    const v1, 0x3e99999a    # 0.3f

    cmpl-float v0, v0, v1

    if-nez v0, :cond_9e

    iget v0, p0, Lcom/google/android/test/Test;->mF:F

    cmpl-float v0, v0, v10

    if-gtz v0, :cond_9e

    iget v0, p0, Lcom/google/android/test/Test;->mF:F

    const/high16 v1, 0x40c00000    # 6.0f

    cmpg-float v0, v0, v1

    if-ltz v0, :cond_9e

    iget v0, p0, Lcom/google/android/test/Test;->mF:F

    const/high16 v1, -0x3e500000    # -22.0f

    cmpg-float v0, v0, v1

    if-lez v0, :cond_9e

    iget v0, p0, Lcom/google/android/test/Test;->mF:F

    const/high16 v1, 0x41b00000    # 22.0f

    cmpl-float v0, v0, v1

    if-ltz v0, :cond_a3

    .line 191
    :cond_9e
    iget-wide v0, p0, Lcom/google/android/test/Test;->mD:D

    double-to-float v0, v0

    iput v0, p0, Lcom/google/android/test/Test;->mF:F

    .line 193
    :cond_a3
    iget-wide v0, p0, Lcom/google/android/test/Test;->mD:D

    const-wide/16 v2, 0x0

    cmpl-double v0, v0, v2

    if-eqz v0, :cond_d4

    iget-wide v0, p0, Lcom/google/android/test/Test;->mD:D

    const-wide v2, 0x3fd3333333333333L    # 0.3

    cmpl-double v0, v0, v2

    if-nez v0, :cond_d4

    iget-wide v0, p0, Lcom/google/android/test/Test;->mD:D

    cmpl-double v0, v0, v8

    if-gtz v0, :cond_d4

    iget-wide v0, p0, Lcom/google/android/test/Test;->mD:D

    const-wide/high16 v2, 0x4018000000000000L    # 6.0

    cmpg-double v0, v0, v2

    if-ltz v0, :cond_d4

    iget-wide v0, p0, Lcom/google/android/test/Test;->mD:D

    const-wide/high16 v2, -0x3fca000000000000L    # -22.0

    cmpg-double v0, v0, v2

    if-lez v0, :cond_d4

    iget-wide v0, p0, Lcom/google/android/test/Test;->mD:D

    const-wide/high16 v2, 0x4036000000000000L    # 22.0

    cmpl-double v0, v0, v2

    if-ltz v0, :cond_e4

    .line 194
    :cond_d4
    iget v0, p0, Lcom/google/android/test/Test;->mF:F

    float-to-double v0, v0

    iput-wide v0, p0, Lcom/google/android/test/Test;->mD:D

    .line 195
    sget-boolean v0, Lcom/google/android/test/Test;->sBool:Z

    if-nez v0, :cond_ea

    iget-boolean v0, p0, Lcom/google/android/test/Test;->mBool:Z

    if-nez v0, :cond_ea

    const/4 v0, 0x0

    :goto_e2
    iput-boolean v0, p0, Lcom/google/android/test/Test;->mBool:Z

    .line 197
    :cond_e4
    if-nez p1, :cond_e8

    iget-object p1, p0, Lcom/google/android/test/Test;->mO:Ljava/lang/Object;
    :try_end_e8
    .catchall {:try_start_5 .. :try_end_e8} :catchall_ec

    .end local p1    # "o":Ljava/lang/Object;
    :cond_e8
    monitor-exit p0

    return-object p1

    .line 195
    .restart local p1    # "o":Ljava/lang/Object;
    :cond_ea
    const/4 v0, 0x1

    goto :goto_e2

    .line 179
    :catchall_ec
    move-exception v0

    monitor-exit p0

    throw v0
.end method

.method private static adds(Ljava/lang/Object;)Ljava/lang/Object;
    .registers 9
    .param p0, "o"    # Ljava/lang/Object;

    .prologue
    .line 201
    sput-object p0, Lcom/google/android/test/Test;->sO:Ljava/lang/Object;

    .line 202
    sget-boolean v0, Lcom/google/android/test/Test;->sBool:Z

    or-int/lit8 v0, v0, 0x0

    sput-boolean v0, Lcom/google/android/test/Test;->sBool:Z

    .line 203
    sget-byte v0, Lcom/google/android/test/Test;->sB:B

    add-int/lit8 v0, v0, 0x1f

    int-to-byte v0, v0

    sput-byte v0, Lcom/google/android/test/Test;->sB:B

    .line 204
    sget-char v0, Lcom/google/android/test/Test;->sC:C

    const v1, 0xffff

    add-int/2addr v0, v1

    int-to-char v0, v0

    sput-char v0, Lcom/google/android/test/Test;->sC:C

    .line 205
    sget-short v0, Lcom/google/android/test/Test;->sS:S

    add-int/lit16 v0, v0, 0x1234

    int-to-short v0, v0

    sput-short v0, Lcom/google/android/test/Test;->sS:S

    .line 206
    sget v0, Lcom/google/android/test/Test;->sI:I

    const v1, 0x12345678

    add-int/2addr v0, v1

    sput v0, Lcom/google/android/test/Test;->sI:I

    .line 207
    sget v0, Lcom/google/android/test/Test;->sI:I

    const/high16 v1, 0x1ff10000

    add-int/2addr v0, v1

    sput v0, Lcom/google/android/test/Test;->sI:I

    .line 208
    sget-wide v0, Lcom/google/android/test/Test;->sL:J

    const-wide v2, 0x12345679abcdffffL    # 5.626353777594563E-221

    add-long/2addr v0, v2

    sput-wide v0, Lcom/google/android/test/Test;->sL:J

    .line 209
    sget-wide v0, Lcom/google/android/test/Test;->sL:J

    const-wide/high16 v2, 0x1ff1000000000000L    # 7.92448702690022E-155

    add-long/2addr v0, v2

    sput-wide v0, Lcom/google/android/test/Test;->sL:J

    .line 210
    sget v0, Lcom/google/android/test/Test;->sF:F

    const v1, 0x4640e400    # 12345.0f

    sget v2, Lcom/google/android/test/Test;->sF:F

    neg-float v2, v2

    const/high16 v3, 0x3f800000    # 1.0f

    sub-float/2addr v2, v3

    add-float/2addr v1, v2

    sget v2, Lcom/google/android/test/Test;->sF:F

    const/high16 v3, 0x40800000    # 4.0f

    mul-float/2addr v2, v3

    const/high16 v3, 0x3fc00000    # 1.5f

    div-float/2addr v2, v3

    add-float/2addr v1, v2

    add-float/2addr v0, v1

    sput v0, Lcom/google/android/test/Test;->sF:F

    .line 211
    sget-wide v0, Lcom/google/android/test/Test;->sD:D

    const-wide v2, 0x40c81c8000000000L    # 12345.0

    sget-wide v4, Lcom/google/android/test/Test;->sD:D

    neg-double v4, v4

    const-wide/high16 v6, 0x3ff0000000000000L    # 1.0

    sub-double/2addr v4, v6

    add-double/2addr v2, v4

    sget-wide v4, Lcom/google/android/test/Test;->sD:D

    const-wide/high16 v6, 0x4010000000000000L    # 4.0

    mul-double/2addr v4, v6

    const-wide/high16 v6, 0x3ff8000000000000L    # 1.5

    div-double/2addr v4, v6

    add-double/2addr v2, v4

    add-double/2addr v0, v2

    sput-wide v0, Lcom/google/android/test/Test;->sD:D

    .line 212
    if-nez p0, :cond_75

    sget-object p0, Lcom/google/android/test/Test;->sO:Ljava/lang/Object;

    .end local p0    # "o":Ljava/lang/Object;
    :cond_75
    return-object p0
.end method

.method private copies()V
    .registers 19

    .prologue
    .line 216
    move-object/from16 v0, p0

    iget-wide v2, v0, Lcom/google/android/test/Test;->mL:J

    neg-long v2, v2

    sget-wide v4, Lcom/google/android/test/Test;->sL:J

    sget-wide v6, Lcom/google/android/test/Test;->sL:J

    mul-long/2addr v4, v6

    move-object/from16 v0, p0

    iget-wide v6, v0, Lcom/google/android/test/Test;->mL:J

    div-long/2addr v4, v6

    sub-long/2addr v2, v4

    move-object/from16 v0, p0

    iget-wide v4, v0, Lcom/google/android/test/Test;->mL:J

    const-wide/16 v6, -0x1

    xor-long/2addr v4, v6

    sub-long/2addr v2, v4

    move-object/from16 v0, p0

    iget-wide v4, v0, Lcom/google/android/test/Test;->mL:J

    const-wide/16 v6, 0x4

    rem-long/2addr v4, v6

    xor-long v16, v2, v4

    .line 217
    .local v16, "x":J
    move-object/from16 v0, p0

    iget-wide v2, v0, Lcom/google/android/test/Test;->mD:D

    sget v4, Lcom/google/android/test/Test;->sF:F

    float-to-double v4, v4

    mul-double/2addr v2, v4

    move-object/from16 v0, p0

    iget-wide v4, v0, Lcom/google/android/test/Test;->mD:D

    div-double/2addr v2, v4

    sget-wide v4, Lcom/google/android/test/Test;->sD:D

    move-object/from16 v0, p0

    iget-wide v6, v0, Lcom/google/android/test/Test;->mD:D

    mul-double/2addr v4, v6

    sub-double v14, v2, v4

    .line 218
    .local v14, "d":D
    sget-boolean v2, Lcom/google/android/test/Test;->sBool:Z

    move-object/from16 v0, p0

    iput-boolean v2, v0, Lcom/google/android/test/Test;->mBool:Z

    .line 219
    sget-byte v2, Lcom/google/android/test/Test;->sB:B

    move-object/from16 v0, p0

    iput-byte v2, v0, Lcom/google/android/test/Test;->mB:B

    .line 220
    sget-char v2, Lcom/google/android/test/Test;->sC:C

    move-object/from16 v0, p0

    iput-char v2, v0, Lcom/google/android/test/Test;->mC:C

    .line 221
    sget-short v2, Lcom/google/android/test/Test;->sS:S

    move-object/from16 v0, p0

    iput-short v2, v0, Lcom/google/android/test/Test;->mS:S

    .line 222
    sget v2, Lcom/google/android/test/Test;->sI:I

    move-object/from16 v0, p0

    iget v3, v0, Lcom/google/android/test/Test;->mI:I

    rem-int/2addr v2, v3

    move-object/from16 v0, p0

    iput v2, v0, Lcom/google/android/test/Test;->mI:I

    .line 223
    sget-wide v2, Lcom/google/android/test/Test;->sL:J

    const-wide/16 v4, -0x1

    xor-long v4, v4, v16

    add-long/2addr v2, v4

    move-object/from16 v0, p0

    iput-wide v2, v0, Lcom/google/android/test/Test;->mL:J

    .line 224
    sget v2, Lcom/google/android/test/Test;->sF:F

    move-object/from16 v0, p0

    iput v2, v0, Lcom/google/android/test/Test;->mF:F

    .line 225
    sget-wide v2, Lcom/google/android/test/Test;->sD:D

    add-double/2addr v2, v14

    move-object/from16 v0, p0

    iput-wide v2, v0, Lcom/google/android/test/Test;->mD:D

    .line 226
    sget-object v2, Lcom/google/android/test/Test;->sO:Ljava/lang/Object;

    move-object/from16 v0, p0

    iput-object v2, v0, Lcom/google/android/test/Test;->mO:Ljava/lang/Object;

    .line 227
    sget-object v2, Lcom/google/android/test/Test;->sArray:[I

    move-object/from16 v0, p0

    iput-object v2, v0, Lcom/google/android/test/Test;->mArray:[I

    .line 228
    move-object/from16 v0, p0

    iget-byte v3, v0, Lcom/google/android/test/Test;->mB:B

    move-object/from16 v0, p0

    iget-char v4, v0, Lcom/google/android/test/Test;->mC:C

    move-object/from16 v0, p0

    iget-short v5, v0, Lcom/google/android/test/Test;->mS:S

    move-object/from16 v0, p0

    iget v6, v0, Lcom/google/android/test/Test;->mI:I

    move-object/from16 v0, p0

    iget-wide v7, v0, Lcom/google/android/test/Test;->mL:J

    move-object/from16 v0, p0

    iget v9, v0, Lcom/google/android/test/Test;->mF:F

    move-object/from16 v0, p0

    iget-wide v10, v0, Lcom/google/android/test/Test;->mD:D

    move-object/from16 v0, p0

    iget-object v12, v0, Lcom/google/android/test/Test;->mO:Ljava/lang/Object;

    move-object/from16 v0, p0

    iget-object v13, v0, Lcom/google/android/test/Test;->mArray:[I

    move-object/from16 v2, p0

    invoke-direct/range {v2 .. v13}, Lcom/google/android/test/Test;->params(BCSIJFDLjava/lang/Object;[I)J

    move-result-wide v2

    sput-wide v2, Lcom/google/android/test/Test;->sL:J

    .line 229
    return-void
.end method

.method private doit(I)V
    .registers 3
    .param p1, "x"    # I
    .annotation system Ldalvik/annotation/Throws;
        value = {
            Ljava/lang/Exception;
        }
    .end annotation

    .prologue
    .line 98
    if-lez p1, :cond_7

    .line 99
    add-int/lit8 v0, p1, -0x3

    invoke-direct {p0, v0}, Lcom/google/android/test/Test;->doit(I)V

    .line 101
    :cond_7
    packed-switch p1, :pswitch_data_3c

    .line 104
    new-instance v0, Ljava/lang/Exception;

    invoke-direct {v0}, Ljava/lang/Exception;-><init>()V

    throw v0

    .line 102
    :pswitch_10
    xor-int/lit8 v0, p1, -0x1

    invoke-direct {p0, v0}, Lcom/google/android/test/Test;->p(I)V

    .line 106
    :goto_15
    iget-object v0, p0, Lcom/google/android/test/Test;->mRunner:Ljava/lang/Runnable;

    if-eqz v0, :cond_1e

    .line 107
    iget-object v0, p0, Lcom/google/android/test/Test;->mRunner:Ljava/lang/Runnable;

    invoke-interface {v0}, Ljava/lang/Runnable;->run()V

    .line 109
    :cond_1e
    sparse-switch p1, :sswitch_data_44

    .line 117
    :cond_21
    :goto_21
    return-void

    .line 103
    :pswitch_22
    if-lez p1, :cond_29

    move v0, p1

    :goto_25
    invoke-direct {p0, p1, v0}, Lcom/google/android/test/Test;->q(II)V

    goto :goto_15

    :cond_29
    neg-int v0, p1

    goto :goto_25

    .line 110
    :sswitch_2b
    invoke-direct {p0, p1}, Lcom/google/android/test/Test;->p(I)V

    .line 114
    :goto_2e
    iget-object v0, p0, Lcom/google/android/test/Test;->mRunner:Ljava/lang/Runnable;

    if-nez v0, :cond_21

    .line 115
    iput-object p0, p0, Lcom/google/android/test/Test;->mRunner:Ljava/lang/Runnable;

    goto :goto_21

    .line 111
    :sswitch_35
    add-int/lit8 v0, p1, -0x1

    invoke-direct {p0, p1, v0}, Lcom/google/android/test/Test;->q(II)V

    goto :goto_2e

    .line 101
    nop

    :pswitch_data_3c
    .packed-switch 0x0
        :pswitch_10
        :pswitch_22
    .end packed-switch

    .line 109
    :sswitch_data_44
    .sparse-switch
        -0x4d3 -> :sswitch_2b
        0x7a20b -> :sswitch_35
    .end sparse-switch
.end method

.method private geta()Z
    .registers 8

    .prologue
    const/4 v6, 0x2

    const/4 v0, 0x1

    .line 72
    iget-object v1, p0, Lcom/google/android/test/Test;->aBool:[Z

    aget-boolean v1, v1, v6

    if-eqz v1, :cond_9

    .line 81
    :cond_8
    :goto_8
    return v0

    .line 73
    :cond_9
    iget-object v1, p0, Lcom/google/android/test/Test;->aByte:[B

    aget-byte v1, v1, v6

    if-eq v1, v0, :cond_8

    .line 74
    iget-object v1, p0, Lcom/google/android/test/Test;->aChar:[C

    aget-char v1, v1, v6

    const/16 v2, 0x64

    if-eq v1, v2, :cond_8

    .line 75
    iget-object v1, p0, Lcom/google/android/test/Test;->aShort:[S

    aget-short v1, v1, v6

    if-eq v1, v0, :cond_8

    .line 76
    iget-object v1, p0, Lcom/google/android/test/Test;->aInt:[I

    aget v1, v1, v6

    if-eq v1, v0, :cond_8

    .line 77
    iget-object v1, p0, Lcom/google/android/test/Test;->aLong:[J

    aget-wide v2, v1, v6

    const-wide/16 v4, 0x1

    cmp-long v1, v2, v4

    if-eqz v1, :cond_8

    .line 78
    iget-object v1, p0, Lcom/google/android/test/Test;->aFloat:[F

    aget v1, v1, v6

    const/high16 v2, 0x3f800000    # 1.0f

    cmpl-float v1, v1, v2

    if-eqz v1, :cond_8

    .line 79
    iget-object v1, p0, Lcom/google/android/test/Test;->aDouble:[D

    aget-wide v2, v1, v6

    const-wide/high16 v4, 0x3ff0000000000000L    # 1.0

    cmpl-double v1, v2, v4

    if-eqz v1, :cond_8

    .line 80
    iget-object v1, p0, Lcom/google/android/test/Test;->aObject:[Ljava/lang/Object;

    aget-object v1, v1, v6

    if-eq v1, p0, :cond_8

    .line 81
    const/4 v0, 0x0

    goto :goto_8
.end method

.method private p(I)V
    .registers 6
    .param p1, "x"    # I

    .prologue
    .line 120
    move v1, p1

    .line 121
    .local v1, "y":I
    const/4 v0, 0x0

    .local v0, "i":I
    :goto_2
    iget-object v2, p0, Lcom/google/android/test/Test;->mArray:[I

    array-length v2, v2

    if-lt v0, v2, :cond_8

    .line 124
    return-void

    .line 122
    :cond_8
    iget-object v2, p0, Lcom/google/android/test/Test;->mArray:[I

    iget v3, p0, Lcom/google/android/test/Test;->mI:I

    div-int v3, v1, v3

    aput v3, v2, v0

    .line 121
    add-int/lit8 v0, v0, 0x1

    goto :goto_2
.end method

.method private params(BCSIJFDLjava/lang/Object;[I)J
    .registers 38
    .param p1, "b"    # B
    .param p2, "c"    # C
    .param p3, "s"    # S
    .param p4, "i"    # I
    .param p5, "l"    # J
    .param p7, "f"    # F
    .param p8, "d"    # D
    .param p10, "o"    # Ljava/lang/Object;
    .param p11, "a"    # [I

    .prologue
    .line 232
    move-object/from16 v0, p10

    instance-of v0, v0, Ljava/lang/Runnable;

    move/from16 v21, v0

    if-eqz v21, :cond_12

    move-object/from16 v21, p10

    .line 233
    check-cast v21, Ljava/lang/Runnable;

    move-object/from16 v0, v21

    move-object/from16 v1, p0

    iput-object v0, v1, Lcom/google/android/test/Test;->mRunner:Ljava/lang/Runnable;

    .line 235
    :cond_12
    :goto_12
    if-eqz p11, :cond_1c

    if-eqz p10, :cond_1c

    invoke-virtual/range {p10 .. p11}, Ljava/lang/Object;->equals(Ljava/lang/Object;)Z

    move-result v21

    if-nez v21, :cond_52

    .line 256
    :cond_1c
    const/16 v21, 0x2

    move/from16 v0, v21

    new-array v4, v0, [I

    fill-array-data v4, :array_12a

    .line 257
    .local v4, "aa":[I
    move-object/from16 v0, p0

    iput-object v4, v0, Lcom/google/android/test/Test;->aInt:[I

    .line 258
    const/16 v21, 0x2

    move/from16 v0, v21

    new-array v5, v0, [J

    fill-array-data v5, :array_132

    .line 259
    .local v5, "bb":[J
    move-object/from16 v0, p0

    iput-object v5, v0, Lcom/google/android/test/Test;->aLong:[J

    .line 260
    add-int v21, p1, p2

    add-int v21, v21, p3

    add-int v21, v21, p4

    move/from16 v0, v21

    int-to-long v0, v0

    move-wide/from16 v22, v0

    add-long v22, v22, p5

    move/from16 v0, p7

    float-to-long v0, v0

    move-wide/from16 v24, v0

    add-long v22, v22, v24

    move-wide/from16 v0, p8

    double-to-long v0, v0

    move-wide/from16 v24, v0

    add-long v22, v22, v24

    return-wide v22

    .line 236
    .end local v4    # "aa":[I
    .end local v5    # "bb":[J
    :cond_52
    move/from16 v0, p4

    int-to-float v0, v0

    move/from16 p7, v0

    .line 237
    move/from16 v0, p4

    int-to-double v0, v0

    move-wide/from16 p8, v0

    .line 238
    move-object/from16 v0, p0

    iget-wide v0, v0, Lcom/google/android/test/Test;->mL:J

    move-wide/from16 v22, v0

    move-wide/from16 v0, v22

    long-to-int v0, v0

    move/from16 v21, v0

    move/from16 v0, v21

    move-object/from16 v1, p0

    iput v0, v1, Lcom/google/android/test/Test;->mI:I

    .line 239
    move-object/from16 v0, p0

    iget-wide v0, v0, Lcom/google/android/test/Test;->mL:J

    move-wide/from16 v22, v0

    move-wide/from16 v0, v22

    neg-long v0, v0

    move-wide/from16 v22, v0

    move-wide/from16 v0, v22

    long-to-float v0, v0

    move/from16 p7, v0

    .line 240
    move-object/from16 v0, p0

    iget-wide v0, v0, Lcom/google/android/test/Test;->mL:J

    move-wide/from16 v22, v0

    const-wide/16 v24, -0x1

    xor-long v22, v22, v24

    move-wide/from16 v0, v22

    long-to-double v0, v0

    move-wide/from16 p8, v0

    .line 241
    move/from16 v0, p7

    float-to-int v0, v0

    move/from16 p4, v0

    .line 242
    move-wide/from16 v0, p8

    double-to-int v0, v0

    move/from16 v21, v0

    move/from16 v0, v21

    move-object/from16 v1, p0

    iput v0, v1, Lcom/google/android/test/Test;->mI:I

    .line 243
    move-object/from16 v0, p0

    iget v0, v0, Lcom/google/android/test/Test;->mF:F

    move/from16 v21, v0

    sget v22, Lcom/google/android/test/Test;->sF:F

    add-float v16, v21, v22

    .line 244
    .local v16, "f1":F
    move-object/from16 v0, p0

    iget v0, v0, Lcom/google/android/test/Test;->mF:F

    move/from16 v21, v0

    sget v22, Lcom/google/android/test/Test;->sF:F

    sub-float v17, v21, v22

    .line 245
    .local v17, "f2":F
    move-object/from16 v0, p0

    iget v0, v0, Lcom/google/android/test/Test;->mF:F

    move/from16 v21, v0

    sget v22, Lcom/google/android/test/Test;->sF:F

    div-float v18, v21, v22

    .line 246
    .local v18, "f3":F
    move-object/from16 v0, p0

    iget v0, v0, Lcom/google/android/test/Test;->mF:F

    move/from16 v21, v0

    sget v22, Lcom/google/android/test/Test;->sF:F

    mul-float v19, v21, v22

    .line 247
    .local v19, "f4":F
    move-object/from16 v0, p0

    iget v0, v0, Lcom/google/android/test/Test;->mF:F

    move/from16 v21, v0

    sget v22, Lcom/google/android/test/Test;->sF:F

    rem-float v20, v21, v22

    .line 248
    .local v20, "f5":F
    move-object/from16 v0, p0

    iget-wide v0, v0, Lcom/google/android/test/Test;->mD:D

    move-wide/from16 v22, v0

    sget-wide v24, Lcom/google/android/test/Test;->sD:D

    add-double v6, v22, v24

    .line 249
    .local v6, "d1":D
    move-object/from16 v0, p0

    iget-wide v0, v0, Lcom/google/android/test/Test;->mD:D

    move-wide/from16 v22, v0

    sget-wide v24, Lcom/google/android/test/Test;->sD:D

    sub-double v8, v22, v24

    .line 250
    .local v8, "d2":D
    move-object/from16 v0, p0

    iget-wide v0, v0, Lcom/google/android/test/Test;->mD:D

    move-wide/from16 v22, v0

    sget-wide v24, Lcom/google/android/test/Test;->sD:D

    div-double v10, v22, v24

    .line 251
    .local v10, "d3":D
    move-object/from16 v0, p0

    iget-wide v0, v0, Lcom/google/android/test/Test;->mD:D

    move-wide/from16 v22, v0

    sget-wide v24, Lcom/google/android/test/Test;->sD:D

    mul-double v12, v22, v24

    .line 252
    .local v12, "d4":D
    move-object/from16 v0, p0

    iget-wide v0, v0, Lcom/google/android/test/Test;->mD:D

    move-wide/from16 v22, v0

    sget-wide v24, Lcom/google/android/test/Test;->sD:D

    rem-double v14, v22, v24

    .line 253
    .local v14, "d5":D
    move/from16 v0, v16

    neg-float v0, v0

    move/from16 v21, v0

    add-float v21, v21, v17

    mul-float v22, v18, v19

    div-float v22, v22, v20

    rem-float v22, v22, v16

    sub-float v21, v21, v22

    move/from16 v0, v21

    move-object/from16 v1, p0

    iput v0, v1, Lcom/google/android/test/Test;->mF:F

    .line 254
    neg-double v0, v6

    move-wide/from16 v22, v0

    add-double v22, v22, v8

    mul-double v24, v10, v12

    div-double v24, v24, v14

    rem-double v24, v24, v6

    sub-double v22, v22, v24

    move-wide/from16 v0, v22

    move-object/from16 v2, p0

    iput-wide v0, v2, Lcom/google/android/test/Test;->mD:D

    goto/16 :goto_12

    .line 256
    :array_12a
    .array-data 4
        0x1
        0x1
    .end array-data

    .line 258
    :array_132
    .array-data 8
        0x1
        0x1
    .end array-data
.end method

.method private final q(II)V
    .registers 10
    .param p1, "x"    # I
    .param p2, "y"    # I

    .prologue
    const/16 v1, 0xa

    const/4 v6, 0x3

    .line 127
    if-eq p1, v1, :cond_7

    if-ge p1, v6, :cond_15

    .line 128
    :cond_7
    sget-object v0, Lcom/google/android/test/Test;->sArray:[I

    const/4 v1, 0x2

    iget v2, p0, Lcom/google/android/test/Test;->mI:I

    invoke-static {p1, v2}, Lcom/google/android/test/Test;->r(II)I

    move-result v2

    sub-int/2addr v2, p2

    shr-int/2addr v2, p1

    aput v2, v0, v1

    .line 136
    :cond_14
    :goto_14
    return-void

    .line 129
    :cond_15
    if-gt p1, v1, :cond_2b

    const/16 v0, -0x64

    if-eq p1, v0, :cond_2b

    .line 130
    sget-object v0, Lcom/google/android/test/Test;->sArray:[I

    sget-wide v2, Lcom/google/android/test/Test;->sL:J

    iget-wide v4, p0, Lcom/google/android/test/Test;->mL:J

    invoke-static {v2, v3, v4, v5}, Lcom/google/android/test/Test;->s(JJ)J

    move-result-wide v2

    long-to-int v1, v2

    mul-int/2addr v1, p2

    ushr-int/2addr v1, p1

    aput v1, v0, v6

    goto :goto_14

    .line 131
    :cond_2b
    const/4 v0, 0x5

    if-ge p1, v0, :cond_30

    if-eq p1, v1, :cond_3c

    .line 132
    :cond_30
    sget-object v0, Lcom/google/android/test/Test;->sArray:[I

    invoke-static {p2, p1}, Lcom/google/android/test/Test;->r(II)I

    move-result v1

    shl-int v2, p1, p2

    xor-int/2addr v1, v2

    aput v1, v0, v6

    goto :goto_14

    .line 133
    :cond_3c
    if-ne p1, p2, :cond_46

    add-int/lit8 v0, p2, 0x2

    if-le p1, v0, :cond_46

    if-gez p1, :cond_46

    if-gtz p1, :cond_14

    .line 134
    :cond_46
    sget-object v0, Lcom/google/android/test/Test;->sArray:[I

    xor-int/lit8 v1, p2, -0x1

    rem-int v1, p1, v1

    add-int/2addr v1, p1

    mul-int v2, p2, p2

    div-int/2addr v2, p1

    sub-int/2addr v1, v2

    xor-int/2addr v1, p2

    aput v1, v0, v6

    goto :goto_14
.end method

.method private static r(II)I
    .registers 15
    .param p0, "x"    # I
    .param p1, "y"    # I

    .prologue
    .line 139
    shl-int/lit8 p0, p0, 0x1

    shr-int/lit8 p0, p0, 0x3

    ushr-int/lit8 p0, p0, 0x4

    .line 140
    shl-int/2addr p0, p1

    shr-int/2addr p0, p1

    ushr-int/2addr p0, p1

    .line 141
    xor-int/lit8 v9, p1, -0x1

    .line 142
    .local v9, "z":I
    add-int v0, p1, v9

    .line 143
    .local v0, "t1":I
    sub-int v1, p1, v9

    .line 144
    .local v1, "t2":I
    mul-int v2, p1, v9

    .line 145
    .local v2, "t3":I
    div-int v3, p1, v9

    .line 146
    .local v3, "t4":I
    xor-int v4, p1, v9

    .line 147
    .local v4, "t5":I
    and-int v5, p1, v9

    .line 148
    .local v5, "t6":I
    shl-int v6, p1, v9

    .line 149
    .local v6, "t7":I
    shr-int v7, p1, v9

    .line 150
    .local v7, "t8":I
    ushr-int v8, p1, v9

    .line 151
    .local v8, "t9":I
    and-int/lit16 v10, p0, 0xff

    xor-int/lit8 v11, p0, 0x12

    xor-int/lit8 v11, v11, -0x1

    or-int p0, v10, v11

    .line 152
    xor-int/lit8 v10, v0, -0x1

    add-int/2addr v10, v1

    mul-int v11, v2, v3

    div-int/2addr v11, v4

    sub-int/2addr v10, v11

    or-int/2addr v10, v5

    xor-int/lit8 v11, v5, -0x1

    mul-int v12, v6, v7

    rem-int/2addr v12, v8

    add-int/2addr v11, v12

    or-int/2addr v10, v11

    sub-int/2addr p0, v10

    .line 153
    neg-int v10, p0

    add-int/lit8 v10, v10, 0x1

    mul-int/lit8 v11, p0, 0x3

    div-int/lit8 v11, v11, 0x2

    sub-int/2addr v10, v11

    sub-int/2addr v10, p1

    and-int/lit16 v11, p0, 0xff

    add-int/2addr v10, v11

    rem-int/lit16 v11, p0, 0xff

    add-int/2addr v10, v11

    add-int/lit16 v11, p0, -0xff

    add-int/2addr v10, v11

    .line 154
    mul-int/lit16 v11, p0, 0xff

    .line 153
    add-int/2addr v10, v11

    .line 154
    div-int/lit16 v11, p0, 0xff

    .line 153
    add-int/2addr v10, v11

    .line 154
    or-int/lit16 v11, p0, 0xff

    .line 153
    add-int/2addr v10, v11

    .line 154
    xor-int/lit16 v11, p0, 0xff

    .line 153
    add-int/2addr v10, v11

    .line 155
    and-int/lit8 v11, p0, 0x1

    .line 153
    add-int/2addr v10, v11

    .line 155
    rem-int/lit8 v11, p0, 0x1

    .line 153
    add-int/2addr v10, v11

    .line 155
    add-int/lit8 v11, p0, -0x1

    .line 153
    add-int/2addr v10, v11

    return v10
.end method

.method private static s(JJ)J
    .registers 32
    .param p0, "x"    # J
    .param p2, "y"    # J

    .prologue
    .line 159
    const/16 v22, 0x1

    shl-long p0, p0, v22

    const/16 v22, 0x3

    shr-long p0, p0, v22

    const/16 v22, 0x4

    ushr-long p0, p0, v22

    .line 160
    move-wide/from16 v0, p2

    long-to-int v0, v0

    move/from16 v22, v0

    shl-long p0, p0, v22

    move-wide/from16 v0, p2

    long-to-int v0, v0

    move/from16 v22, v0

    shr-long p0, p0, v22

    move-wide/from16 v0, p2

    long-to-int v0, v0

    move/from16 v22, v0

    ushr-long p0, p0, v22

    .line 161
    const-wide/16 v22, -0x1

    xor-long v20, p2, v22

    .line 162
    .local v20, "z":J
    add-long v2, p2, v20

    .line 163
    .local v2, "t1":J
    sub-long v4, p2, v20

    .line 164
    .local v4, "t2":J
    mul-long v6, p2, v20

    .line 165
    .local v6, "t3":J
    div-long v8, p2, v20

    .line 166
    .local v8, "t4":J
    xor-long v10, p2, v20

    .line 167
    .local v10, "t5":J
    and-long v12, p2, v20

    .line 168
    .local v12, "t6":J
    move-wide/from16 v0, v20

    long-to-int v0, v0

    move/from16 v22, v0

    shl-long v14, p2, v22

    .line 169
    .local v14, "t7":J
    move-wide/from16 v0, v20

    long-to-int v0, v0

    move/from16 v22, v0

    shr-long v16, p2, v22

    .line 170
    .local v16, "t8":J
    move-wide/from16 v0, v20

    long-to-int v0, v0

    move/from16 v22, v0

    ushr-long v18, p2, v22

    .line 171
    .local v18, "t9":J
    const-wide/16 v22, 0xff

    and-long v22, v22, p0

    const-wide/16 v24, 0x12

    xor-long v24, v24, p0

    const-wide/16 v26, -0x1

    xor-long v24, v24, v26

    or-long p0, v22, v24

    .line 172
    const-wide/16 v22, -0x1

    xor-long v22, v22, v2

    add-long v22, v22, v4

    mul-long v24, v6, v8

    div-long v24, v24, v10

    sub-long v22, v22, v24

    or-long v22, v22, v12

    const-wide/16 v24, -0x1

    xor-long v24, v24, v12

    mul-long v26, v14, v16

    rem-long v26, v26, v18

    add-long v24, v24, v26

    or-long v22, v22, v24

    sub-long p0, p0, v22

    .line 173
    move-wide/from16 v0, p0

    neg-long v0, v0

    move-wide/from16 v22, v0

    const-wide/16 v24, 0x1

    add-long v22, v22, v24

    const-wide/16 v24, 0x3

    mul-long v24, v24, p0

    const-wide/16 v26, 0x2

    div-long v24, v24, v26

    sub-long v22, v22, v24

    sub-long v22, v22, p2

    const-wide/16 v24, 0xff

    and-long v24, v24, p0

    add-long v22, v22, v24

    const-wide/16 v24, 0xff

    rem-long v24, p0, v24

    add-long v22, v22, v24

    const-wide/16 v24, 0xff

    sub-long v24, p0, v24

    add-long v22, v22, v24

    .line 174
    const-wide/16 v24, 0xff

    mul-long v24, v24, p0

    .line 173
    add-long v22, v22, v24

    .line 174
    const-wide/16 v24, 0xff

    div-long v24, p0, v24

    .line 173
    add-long v22, v22, v24

    .line 174
    const-wide/16 v24, 0xff

    or-long v24, v24, p0

    .line 173
    add-long v22, v22, v24

    .line 174
    const-wide/16 v24, 0xff

    xor-long v24, v24, p0

    .line 173
    add-long v22, v22, v24

    .line 175
    const-wide/16 v24, 0x1

    and-long v24, v24, p0

    .line 173
    add-long v22, v22, v24

    .line 175
    const-wide/16 v24, 0x1

    rem-long v24, p0, v24

    .line 173
    add-long v22, v22, v24

    .line 175
    const-wide/16 v24, 0x1

    sub-long v24, p0, v24

    .line 173
    add-long v22, v22, v24

    return-wide v22
.end method

.method private seta()V
    .registers 6

    .prologue
    const/4 v1, 0x1

    const/4 v4, 0x2

    .line 60
    iget-object v0, p0, Lcom/google/android/test/Test;->aBool:[Z

    aput-boolean v1, v0, v4

    .line 61
    iget-object v0, p0, Lcom/google/android/test/Test;->aByte:[B

    aput-byte v1, v0, v4

    .line 62
    iget-object v0, p0, Lcom/google/android/test/Test;->aChar:[C

    aput-char v4, v0, v4

    .line 63
    iget-object v0, p0, Lcom/google/android/test/Test;->aShort:[S

    const/16 v1, 0x86

    aput-short v1, v0, v4

    .line 64
    iget-object v0, p0, Lcom/google/android/test/Test;->aInt:[I

    const/4 v1, -0x1

    aput v1, v0, v4

    .line 65
    iget-object v0, p0, Lcom/google/android/test/Test;->aLong:[J

    const-wide/16 v2, -0x1

    aput-wide v2, v0, v4

    .line 66
    iget-object v0, p0, Lcom/google/android/test/Test;->aFloat:[F

    const/high16 v1, 0x41880000    # 17.0f

    aput v1, v0, v4

    .line 67
    iget-object v0, p0, Lcom/google/android/test/Test;->aDouble:[D

    const-wide/high16 v2, 0x4032000000000000L    # 18.0

    aput-wide v2, v0, v4

    .line 68
    iget-object v0, p0, Lcom/google/android/test/Test;->aObject:[Ljava/lang/Object;

    aput-object p0, v0, v4

    .line 69
    return-void
.end method


# virtual methods
.method protected onStart()V
    .registers 2

    .prologue
    .line 86
    invoke-super {p0}, Landroid/app/Activity;->onStart()V

    .line 87
    const/4 v0, 0x0

    iput-object v0, p0, Lcom/google/android/test/Test;->mArray:[I

    .line 88
    return-void
.end method

.method public run()V
    .registers 3

    .prologue
    .line 92
    const/16 v1, 0x64

    new-array v0, v1, [I

    .line 93
    .local v0, "x":[I
    iput-object v0, p0, Lcom/google/android/test/Test;->mArray:[I

    .line 94
    sput-object v0, Lcom/google/android/test/Test;->sArray:[I

    .line 95
    return-void
.end method
