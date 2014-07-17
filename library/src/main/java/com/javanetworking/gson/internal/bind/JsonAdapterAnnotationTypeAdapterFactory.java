/*
 * Copyright (C) 2014 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.gson.internal.bind;

import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.annotations.JsonAdapter;
import com.google.gson.internal.ConstructorConstructor;
import com.google.gson.internal.ObjectConstructor;
import com.google.gson.reflect.TypeToken;

/**
 * Given a type T, looks for the annotation {@link JsonAdapter} and uses an instance of the
 * specified class as the default type adapter.
 *
 * @since 2.3
 */
public final class JsonAdapterAnnotationTypeAdapterFactory implements TypeAdapterFactory {

  private final ConstructorConstructor constructorConstructor;

  public JsonAdapterAnnotationTypeAdapterFactory(ConstructorConstructor constructorConstructor) {
    this.constructorConstructor = constructorConstructor;
  }

  @SuppressWarnings({ "rawtypes", "unchecked" })
  public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> targetType) {
    Class<? super T> clazz = targetType.getRawType();
    JsonAdapter annotation = clazz.getAnnotation(JsonAdapter.class);
    if (annotation == null) return null;
    TypeAdapter adapter = getAnnotationTypeAdapter(gson, constructorConstructor, annotation);
    return adapter;
  }

  static TypeAdapter<?> getAnnotationTypeAdapter(Gson gson,
      ConstructorConstructor constructorConstructor, JsonAdapter annotation) {
    Class<? extends TypeAdapter<?>> adapterClass = annotation.value();
    ObjectConstructor<? extends TypeAdapter<?>> constructor =
        constructorConstructor.get(TypeToken.get(adapterClass));
    TypeAdapter<?> adapter = constructor.construct();
    Gson.$$Internal.addGeneratedTypeAdapter(gson, adapter);
    return adapter;
  }
}
