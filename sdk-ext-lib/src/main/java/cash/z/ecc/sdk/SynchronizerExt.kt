package cash.z.ecc.sdk

import android.content.Context
import cash.z.ecc.android.bip39.Mnemonics
import cash.z.ecc.android.bip39.toSeed
import cash.z.ecc.android.sdk.Initializer
import cash.z.ecc.android.sdk.Synchronizer
import cash.z.ecc.android.sdk.tool.DerivationTool
import cash.z.ecc.android.sdk.type.UnifiedViewingKey
import cash.z.ecc.sdk.model.PersistableWallet
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

suspend fun Synchronizer.Companion.new(context: Context, persistableWallet: PersistableWallet): Synchronizer {
    val config = persistableWallet.toConfig()
    val initializer = Initializer.new(context, config)
    return Synchronizer.new(initializer)
}

private suspend fun PersistableWallet.deriveViewingKey(): UnifiedViewingKey {
    // Dispatcher needed because SecureRandom is loaded, which is slow and performs IO
    // https://github.com/zcash/kotlin-bip39/issues/13
    val bip39Seed = withContext(Dispatchers.IO) {
        Mnemonics.MnemonicCode(seedPhrase.joinToString()).toSeed()
    }

    return DerivationTool.deriveUnifiedViewingKeys(bip39Seed, network)[0]
}

private suspend fun PersistableWallet.toConfig(): Initializer.Config {
    val network = network
    val vk = deriveViewingKey()

    return Initializer.Config {
        it.importWallet(vk, birthday?.height, network, network.defaultHost, network.defaultPort)
    }
}
