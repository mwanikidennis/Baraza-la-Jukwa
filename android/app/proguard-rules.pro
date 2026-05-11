# ============================================================================
# JUKWA Android App - ProGuard / R8 Rules
# ============================================================================

# ----------------------------------------------------------------------------
# 1. General Android / Model Rules
# ----------------------------------------------------------------------------

# Keep application model classes (data classes, entities, DTOs) used by
# serialization, Room, Hilt, etc.  Annotate with @Keep or list explicitly
# if a broader rule is too aggressive.
-keepattributes *Annotation*
-keepattributes Signature
-keepattributes Exceptions
-keepattributes InnerClasses
-keepattributes EnclosingMethod
-keepattributes SourceFile,LineNumberTable

# Preserve source file names + line numbers for crash reports
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# Keep all model/entity classes in the app's model package
-keep class ke.jukwa.data.model.** { *; }
-keep class ke.jukwa.data.entity.** { *; }
-keep class ke.jukwa.data.dto.** { *; }
-keep class ke.jukwa.domain.model.** { *; }

# Keep classes annotated with @Keep
-keep @interface androidx.annotation.Keep
-keep class * { @androidx.annotation.Keep *; }

# ----------------------------------------------------------------------------
# 2. Hilt / Dagger Rules
# ----------------------------------------------------------------------------

# Keep @Inject constructors so Hilt can create instances
-keepclassmembers,allowobfuscation class * {
    @javax.inject.Inject <init>(...);
}

# Keep @Module classes and their @Provides / @Binds methods
-keep @dagger.Module class * { *; }
-keep @dagger.hilt.InstallIn class * { *; }
-keep @dagger.hilt.android.EntryPoint class * { *; }

-keepclassmembers class * {
    @dagger.Provides *;
    @dagger.Binds *;
}

# Keep Hilt generated components
-keep class **_HiltModules* { *; }
-keep class **_HiltComponents* { *; }
-keep class **_GeneratedInjector { *; }
-keep class **_HiltAdapters { *; }

# Keep Hilt application and entry point classes
-keep class * extends dagger.hilt.android.Hilt_App { *; }
-keep @dagger.hilt.android.lifecycle.HiltViewModel class * { *; }

# Keep classes annotated with Hilt scope annotations
-keep @dagger.hilt.android.scopes.ActivityScoped class * { *; }
-keep @dagger.hilt.android.scopes.FragmentScoped class * { *; }
-keep @dagger.hilt.android.scopes.ViewScoped class * { *; }
-keep @dagger.hilt.android.scopes.ServiceScoped class * { *; }
-keep @dagger.hilt.android.scopes.ViewWithFragmentScoped class * { *; }

# Keep the Hilt Worker factory
-keep class * extends androidx.hilt.work.HiltWorker { *; }
-keepclassmembers class * extends androidx.hilt.work.HiltWorker {
    @javax.inject.Inject <init>(android.content.Context, androidx.work.WorkerParameters);
}

# Keep member injectors
-keepclassmembers class * {
    @dagger.hilt.android.lifecycle.HiltViewModel <init>(...);
}

# Dagger internal rules
-dontwarn dagger.**
-keep class dagger.** { *; }
-keep class javax.inject.** { *; }
-keep class dagger.hilt.** { *; }

# ----------------------------------------------------------------------------
# 3. Room Database Rules
# ----------------------------------------------------------------------------

# Keep Room entities (classes annotated with @Entity)
-keep @androidx.room.Entity class * { *; }

# Keep Room DAOs (interfaces/classes annotated with @Dao)
-keep @androidx.room.Dao class * { *; }
-keep class * extends androidx.room.RoomDatabase { *; }

# Keep all fields in Room entities (Room accesses fields by name)
-keepclassmembers @androidx.room.Entity class * {
    <fields>;
}

# Keep primary key fields
-keepclassmembers @androidx.room.Entity class * {
    @androidx.room.PrimaryKey <fields>;
}

# Keep ignored fields (still need to be visible for annotation processing)
-keepclassmembers @androidx.room.Entity class * {
    @androidx.room.Ignore *;
}

# Keep Embedded fields
-keepclassmembers @androidx.room.Entity class * {
    @androidx.room.Embedded <fields>;
}

# Keep Relation fields
-keepclassmembers @androidx.room.Entity class * {
    @androidx.room.Relation <fields>;
}

# Keep type converter classes
-keep @androidx.room.TypeConverters class * { *; }
-keepclassmembers @androidx.room.TypeConverters class * {
    *;
}

# Keep Database class and its abstract methods
-keepclassmembers class * extends androidx.room.RoomDatabase {
    <methods>;
}

# Keep DAO interface methods
-keepclassmembers @androidx.room.Dao interface * {
    <methods>;
}

# Keep POJO classes used as return types in Room queries
-keep class * extends java.lang.AutoCloseable { *; }

# Undocumented Room internal helpers
-keep class * extends androidx.room.AndroidXRoomDatabase { *; }
-dontwarn androidx.room.paging.**

# ----------------------------------------------------------------------------
# 4. Kotlin Coroutines Rules
# ----------------------------------------------------------------------------

# Keep coroutine continuation classes (used by state machines)
-keepclassmembers class kotlinx.coroutines.** {
    **;
}

# Keep coroutine state machine internals
-keepclassmembers class * {
    ** Continuation;
    ** ContinuationInterceptor;
}

# Keep coroutine internal classes
-keep class kotlinx.coroutines.** { *; }
-keep class kotlin.coroutines.** { *; }
-keep class kotlin.coroutines.intrinsics.** { *; }

# Keep continuation label fields used by generated state machines
-keepclassmembers class * {
    int label;
    java.lang.Object continuation;
}

# Coroutine flow
-keep class kotlinx.coroutines.flow.** { *; }

# Suppress warnings for coroutine experimental APIs
-dontwarn kotlinx.coroutines.**
-dontwarn kotlinx.serialization.**

# ----------------------------------------------------------------------------
# 5. Kotlin Serialization Rules
# ----------------------------------------------------------------------------

# Keep @Serializable classes and their fields
-keepattributes *Annotation*, SerializationExt

# Keep classes with @Serializable annotation
-keep @kotlinx.serialization.Serializable class * { *; }

# Keep the companion serializer field
-keepclassmembers @kotlinx.serialization.Serializable class * {
    static *** Companion;
    *** Companion;
}

# Keep serializer objects
-keep class * implements kotlinx.serialization.KSerializer {
    <init>(...);
    <methods>;
}

# Keep SERIALIZER field in companion objects
-keepclassmembers class * {
    *** Companion;
    *** Companion$getSERIALIZER$...;
}

# Keep serialization internal machinery
-keepclassmembers class kotlinx.serialization.** {
    <methods>;
}

# Keep generated serializers
-keep class *$$serializer { *; }
-keepclassmembers class *$$serializer {
    <fields>;
    <methods>;
}

# Platform-specific serialization
-keep class kotlinx.serialization.json.** { *; }
-keep class kotlinx.serialization.builtins.** { *; }

-dontwarn kotlinx.serialization.**

# ----------------------------------------------------------------------------
# 6. Ktor Client Rules
# ----------------------------------------------------------------------------

# Keep Ktor client plugins and configurations
-keep class io.ktor.** { *; }
-keep interface io.ktor.** { *; }

# Keep Ktor serializable request/response classes
-keepclassmembers class * {
    @kotlinx.serialization.Serializable *;
}

# Keep Ktor client feature / plugin configurations
-keep class * implements io.ktor.client.plugins.* {
    <init>(...);
}

# Keep content negotiation converters
-keep class io.ktor.serialization.kotlinx.json.** { *; }

# Keep Ktor logging
-keep class io.ktor.client.plugins.logging.** { *; }

# Ktor internal engine
-keep class io.ktor.client.engine.android.** { *; }

-dontwarn io.ktor.**

# ----------------------------------------------------------------------------
# 7. MapLibre Native SDK Rules
# ----------------------------------------------------------------------------

# Keep MapLibre native bridge classes with JNI methods
-keep class org.maplibre.android.** { *; }
-keep class org.maplibre.gl.** { *; }

# Keep classes with native methods used by MapLibre
-keepclasseswithmembernames class * {
    native <methods>;
}

# Specifically keep MapLibre core native method holders
-keep class org.maplibre.android.maps.NativeMapView { *; }
-keep class org.maplibre.android.style.layers.** { *; }
-keep class org.maplibre.android.style.sources.** { *; }
-keep class org.maplibre.android.style.expressions.** { *; }
-keep class org.maplibre.android.style.light.** { *; }
-keep class org.maplibre.android.geometry.** { *; }

# Keep MapLibre annotation classes
-keep class org.maplibre.android.annotations.** { *; }

# Keep MapLibre location component
-keep class org.maplibre.android.location.** { *; }

-dontwarn org.maplibre.android.**
-dontwarn org.maplibre.gl.**

# ----------------------------------------------------------------------------
# 8. Coil Image Loading Rules
# ----------------------------------------------------------------------------

# Coil uses reflection for fetching ImageView extensions
-keep class coil.** { *; }
-keep interface coil.** { *; }

# Keep Coil's ImageLoader and keyer classes
-keep class * extends coil.ComponentRegistry$Builder { *; }
-keep class * implements coil.key.Keyer { *; }
-keep class * implements coil.fetch.Fetcher { *; }
-keep class * implements coil.decode.Decoder { *; }

# Keep Coil's @Deprecated transitional APIs if used
-dontwarn coil.**

# ----------------------------------------------------------------------------
# 9. WorkManager Rules
# ----------------------------------------------------------------------------

# Keep Worker classes (referenced by fully-qualified class name)
-keep class * extends androidx.work.Worker { *; }
-keep class * extends androidx.work.CoroutineWorker { *; }
-keep class * extends androidx.work.RxWorker { *; }
-keep class * extends androidx.work.ListenableWorker { *; }

# Keep Hilt Worker classes
-keep class * extends androidx.hilt.work.HiltWorker { *; }

# Keep default constructor for Workers
-keepclassmembers class * extends androidx.work.Worker {
    <init>(android.content.Context, androidx.work.WorkerParameters);
}
-keepclassmembers class * extends androidx.work.CoroutineWorker {
    <init>(android.content.Context, androidx.work.WorkerParameters);
}
-keepclassmembers class * extends androidx.hilt.work.HiltWorker {
    @javax.inject.Inject <init>(android.content.Context, androidx.work.WorkerParameters);
}

# Keep WorkManager's internal room database (it uses Room internally)
-keep class androidx.work.impl.WorkDatabase { *; }

# Keep WorkManager's model classes
-keep class androidx.work.impl.model.** { *; }

# Keep DelegatingWorker (used by Hilt)
-keep class androidx.hilt.work.HiltWorkerFactory { *; }

-dontwarn androidx.work.**

# ----------------------------------------------------------------------------
# 10. DataStore Rules
# ----------------------------------------------------------------------------

# Keep DataStore preferences classes
-keep class androidx.datastore.** { *; }
-keep class * extends com.google.protobuf.GeneratedMessageLite { *; }

# Keep serialized preference keys
-keepclassmembers class * {
    androidx.datastore.preferences.core.PreferencesKey<*>;
}

-dontwarn androidx.datastore.**

# ----------------------------------------------------------------------------
# 11. Lazysodium (NaCl/Sodium) Rules
# ----------------------------------------------------------------------------

# Keep Lazysodium JNI bridge classes
-keep class com.goterl.lazysodium.** { *; }
-keep interface com.goterl.lazysodium.** { *; }

# Keep native method names (JNI relies on exact method signatures)
-keepclasseswithmembernames,includedescriptorclasses class * {
    native <methods>;
}

# Keep Lazysodium Sodium class (core JNI bridge)
-keep class com.goterl.lazysodium.Sodium { *; }
-keep class com.goterl.lazysodium.SodiumJava { *; }
-keep class com.goterl.lazysodium.utils.** { *; }

# Keep key and nonce classes
-keep class com.goterl.lazysodium.utils.Key { *; }
-keep class com.goterl.lazysodium.utils.KeyPair { *; }
-keep class com.goterl.lazysodium.utils.Nonce { *; }

# Keep all enums used by Lazysodium (for return types and parameters)
-keepclassmembers enum com.goterl.lazysodium.** {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

-dontwarn com.goterl.lazysodium.**

# ----------------------------------------------------------------------------
# 12. HiveMQ MQTT Client Rules
# ----------------------------------------------------------------------------

# Keep HiveMQ MQTT client classes
-keep class com.hivemq.client.** { *; }
-keep interface com.hivemq.client.** { *; }

# Keep MQTT callback interfaces (referenced by reflection)
-keepclassmembers interface com.hivemq.client.mqtt.** {
    <methods>;
}

# Keep MQTT message and topic classes
-keep class com.hivemq.client.mqtt.datatypes.** { *; }
-keep class com.hivemq.client.mqtt.mqtt5.message.** { *; }

# Keep MQTT publish callback implementations
-keepclassmembers class * implements com.hivemq.client.mqtt.MqttClient {
    <methods>;
}

# Keep MQTT connection listeners
-keepclassmembers class * implements com.hivemq.client.mqtt.lifecycle.MqttClientDisconnectedListener {
    <methods>;
}
-keepclassmembers class * implements com.hivemq.client.mqtt.lifecycle.MqttClientConnectedListener {
    <methods>;
}

# Keep MQTT subscribe listeners
-keepclassmembers class * implements com.hivemq.client.mqtt.mqtt5.callback.Mqtt5PublishListener {
    <methods>;
}

# Keep HiveMQ internal buffer / netty classes
-keep class com.hivemq.client.internal.** { *; }
-dontwarn com.hivemq.client.internal.**

# HiveMQ uses Netty
-keep class io.netty.** { *; }
-dontwarn io.netty.**

# HiveMQ uses ByteBuffer
-keep class java.nio.ByteBuffer { *; }

# ----------------------------------------------------------------------------
# 13. Kotlin General Rules
# ----------------------------------------------------------------------------

# Keep Kotlin metadata for reflection
-keepattributes RuntimeVisibleAnnotations
-keep class kotlin.Metadata { *; }

# Keep Kotlin companion objects
-keepclassmembers class * {
    ** Companion;
}

# Keep Kotlin null checks
-keepclassmembers class kotlin.jvm.internal.** { *; }

# Keep Kotlin extension function signatures
-keepclassmembers class * {
    @kotlin.ExtensionFunctionType *;
}

# Keep Kotlin @JvmStatic methods
-keepclassmembers class * {
    @kotlin.jvm.JvmStatic *;
}

# Keep Kotlin @JvmField fields
-keepclassmembers class * {
    @kotlin.jvm.JvmField <fields>;
}

# Keep Kotlin @JvmName methods
-keepclassmembers class * {
    @kotlin.jvm.JvmName *;
}

# Keep Kotlin @JvmOverloads methods
-keepclassmembers class * {
    @kotlin.jvm.JvmOverloads *;
}

# Keep enum values() and valueOf() for all enums
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# Kotlin reflection
-keep class kotlin.reflect.** { *; }
-dontwarn kotlin.reflect.**

# ----------------------------------------------------------------------------
# 14. R8 Optimization Rules
# ----------------------------------------------------------------------------

# Optimize code
-optimizationpasses 5
-optimizations !code/simplification/arithmetic,!code/simplification/cast,!field/*,!class/merging/*

# Don't use mixed class renaming (causes issues with some libraries)
-dontusemixedcaseclassnames

# Don't skip non-public library classes
-dontskipnonpubliclibraryclasses

# Verbose output during processing
-verbose

# Keep annotations used for debugging
-keepattributes RuntimeVisibleAnnotations,RuntimeInvisibleAnnotations
-keepattributes AnnotationDefault

# Suppress warnings for common third-party libraries
-dontwarn com.google.errorprone.**
-dontwarn org.codehaus.**
-dontwarn javax.annotation.**
-dontwarn org.checkerframework.**
-dontwarn org.conscrypt.**

# ----------------------------------------------------------------------------
# 15. Firebase Cloud Messaging Rules
# ----------------------------------------------------------------------------

# Keep Firebase messaging service subclasses
-keep class * extends com.google.firebase.messaging.FirebaseMessagingService { *; }

# Keep Firebase components (discovered by reflection)
-keep class com.google.firebase.** { *; }
-keep class com.google.android.gms.** { *; }

-dontwarn com.google.firebase.**
-dontwarn com.google.android.gms.**

# ----------------------------------------------------------------------------
# 16. AndroidX General Rules
# ----------------------------------------------------------------------------

# Keep AndroidX lifecycle observers
-keep class androidx.lifecycle.** { *; }
-dontwarn androidx.lifecycle.**

# Keep AndroidX compose
-keep class androidx.compose.** { *; }
-dontwarn androidx.compose.**

# Keep AndroidX security crypto
-keep class androidx.security.crypto.** { *; }
-dontwarn androidx.security.crypto.**

# Keep AndroidX ExifInterface
-keep class androidx.exifinterface.** { *; }
-dontwarn androidx.exifinterface.**
