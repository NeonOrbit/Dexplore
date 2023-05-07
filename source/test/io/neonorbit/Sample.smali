# Copyright (C) 2023 NeonOrbit
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

.class public Lio/neonorbit/Sample;
.super Lio/neonorbit/SampleSuper;
.source "Sample.java"

# interfaces
.implements Lio/neonorbit/SampleIFace;


# annotations
.annotation runtime Lio/neonorbit/SampleAnnotation;
    value = "sample class"
.end annotation


# static fields
.field public static final A:I = 0x3f2 # int: 1010

.field public static final B:J = 0xc9L # long: 201L

.field public static final C:F = 301.0f # float: 301f

.field public static final D:D = 401.0 # double: 401d

.field public static final E:D = 501.55 # double: 501.55

.field public static final S:Ljava/lang/String; = "A unique string"


# instance fields
.field public TITLE:Ljava/lang/String;


# direct methods
.method public constructor <init>()V
    .registers 2

    .line 6
    invoke-direct {p0}, Lio/neonorbit/SampleSuper;-><init>()V

    .line 7
    const-string v0, "Dex Samples"

    iput-object v0, p0, Lio/neonorbit/Sample;->TITLE:Ljava/lang/String;

    return-void
.end method


# --- virtual methods --- #
# ----------------------- #


#  public <T> T generic(T param) {
#      return param;
#  }
.method public generic(Ljava/lang/Object;)Ljava/lang/Object;
    .registers 2
    .annotation system Ldalvik/annotation/Signature;
        value = {
            "<T:",
            "Ljava/lang/Object;",
            ">(TT;)TT;"
        }
    .end annotation

    .line 20
    return-object p1
.end method


#  public String getTitle() {
#      return TITLE;
#  }
.method public getTitle()Ljava/lang/String;
    .registers 2

    .line 16
    iget-object v0, p0, Lio/neonorbit/Sample;->TITLE:Ljava/lang/String;

    return-object v0
.end method


#  public void numberSample() {
#      System.out.println(1010);
#      ........................
#  }
.method public numberSample()V
    .registers 4

    .line 30
    sget-object v0, Ljava/lang/System;->out:Ljava/io/PrintStream;

    const/16 v1, 0x3f2    # 1010

    invoke-virtual {v0, v1}, Ljava/io/PrintStream;->println(I)V

    .line 31
    sget-object v0, Ljava/lang/System;->out:Ljava/io/PrintStream;

    const-wide/16 v1, 0xc9    # 201L

    invoke-virtual {v0, v1, v2}, Ljava/io/PrintStream;->println(J)V

    .line 32
    sget-object v0, Ljava/lang/System;->out:Ljava/io/PrintStream;

    const v1, 0x43968000    # 301f

    invoke-virtual {v0, v1}, Ljava/io/PrintStream;->println(F)V

    .line 33
    sget-object v0, Ljava/lang/System;->out:Ljava/io/PrintStream;

    const-wide v1, 0x4079100000000000L    # 401d

    invoke-virtual {v0, v1, v2}, Ljava/io/PrintStream;->println(D)V

    .line 34
    sget-object v0, Ljava/lang/System;->out:Ljava/io/PrintStream;

    const-wide v1, 0x407f58cccccccccdL    # 501.55

    invoke-virtual {v0, v1, v2}, Ljava/io/PrintStream;->println(D)V

    .line 35
    return-void
.end method


#  @SampleAnnotation("sample method")
#  public void receive(Object input) {
#      File file = (File) input;
#      System.out.println("A method sample");
#  }
.method public receive(Ljava/lang/Object;)V
    .registers 3
    .annotation runtime Lio/neonorbit/SampleAnnotation;
        value = "sample method"
    .end annotation

    .line 25
    check-cast p1, Ljava/io/File;

    .line 26
    sget-object p1, Ljava/lang/System;->out:Ljava/io/PrintStream;

    const-string v0, "A method sample"

    invoke-virtual {p1, v0}, Ljava/io/PrintStream;->println(Ljava/lang/String;)V

    .line 27
    return-void
.end method
