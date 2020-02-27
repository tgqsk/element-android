/*
 * Copyright 2020 New Vector Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package im.vector.matrix.android.internal.crypto.crosssigning

import android.util.Base64
import im.vector.matrix.android.internal.crypto.model.CryptoCrossSigningKey
import im.vector.matrix.android.internal.crypto.model.CryptoDeviceInfo
import im.vector.matrix.android.internal.util.JsonCanonicalizer

fun CryptoDeviceInfo.canonicalSignable(): String {
    return JsonCanonicalizer.getCanonicalJson(Map::class.java, signalableJSONDictionary())
}

fun CryptoCrossSigningKey.canonicalSignable(): String {
    return JsonCanonicalizer.getCanonicalJson(Map::class.java, signalableJSONDictionary())
}

fun ByteArray.toBase64NoPadding(): String {
    return Base64.encodeToString(this, Base64.NO_PADDING or Base64.NO_WRAP)
}

fun String.fromBase64NoPadding(): ByteArray {
    return Base64.decode(this, Base64.DEFAULT)
}
