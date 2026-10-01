package Shirakawa.LocalCueWord.auth

/** Global bridge for download worker access to HF credentials. */
object HFTokenManagerHolder {
    @Volatile var instance: HFTokenManager? = null
}
