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

# Class: io.github.neonorbit.dexplore.Main
.class public Lio/github/neonorbit/dexplore/Main;

# Super: java.lang.Object
.super Ljava/lang/Object;


# Direct method
# Constructor: Main()
.method public constructor <init>()V
    .registers 1

    .line 3
    # super() -> Object()
    invoke-direct {p0}, Ljava/lang/Object;-><init>()V

    return-void
.end method

# Direct method
# Method: public static void main(String[] args)
.method public static main([Ljava/lang/String;)V
    .registers 2
    .param p0, "args"   # String[] args

    .line 5
    # new Sample
    new-instance v0, Lio/github/neonorbit/dexplore/Sample;

    # Sample()
    invoke-direct {v0}, Lio/github/neonorbit/dexplore/Sample;-><init>()V

    .line 6
    # sample.hello()
    .local v0, "sample":Lio/github/neonorbit/dexplore/Sample;
    invoke-virtual {v0}, Lio/github/neonorbit/dexplore/Sample;->hello()V

    .line 7
    return-void
.end method
