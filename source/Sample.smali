# Copyright (C) 2022 NeonOrbit
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

# Class: io.github.neonorbit.dexplore.Sample
.class public Lio/github/neonorbit/dexplore/Sample;

# Super: io.github.neonorbit.dexplore.SampleSuper
.super Lio/github/neonorbit/dexplore/SampleSuper;


# Direct method
# Constructor: Sample()
.method public constructor <init>()V
    .registers 1

    .line 3
    # super() -> SampleSuper()
    invoke-direct {p0}, Lio/github/neonorbit/dexplore/SampleSuper;-><init>()V

    return-void
.end method


# Virtual method
# Method: public void hello()
.method public hello()V
    .registers 3

    .line 6
    # Field: System.out
    sget-object v0, Ljava/lang/System;->out:Ljava/io/PrintStream;

    # String: "Hello Smali"
    const-string v1, "Hello Smali"

    # Method: v0.println(v1) ->  System.out.println("Hello Smali")
    invoke-virtual {v0, v1}, Ljava/io/PrintStream;->println(Ljava/lang/String;)V

    .line 7
    return-void
.end method
