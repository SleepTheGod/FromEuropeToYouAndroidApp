package com.example.security

data class SslCertificateInfo(
    val domain: String = "www.fromeuropetoyou.com",
    val isSecure: Boolean = true,
    val isPinned: Boolean = true,
    val protocol: String = "TLSv1.3",
    val cipherSuite: String = "TLS_AES_256_GCM_SHA384",
    val commonName: String = "www.fromeuropetoyou.com",
    val organization: String = "From Europe To You Antiques",
    val issuerName: String = "ISRG Root X1 / GTS CA 1P5",
    val validFrom: String = "2024-01-01",
    val validTo: String = "2027-12-31",
    val sha256Fingerprint: String = "C5:lp:Z7:tc:Vw:mw:QI:Mc:Rt:Pb:sQ:tW:LA:BX:hQ:ze:jn:a0:wH:Fr:8M",
    val pinningStatus: String = "VERIFIED_ACTIVE",
    val mitmDefenseActive: Boolean = true,
    val keyExchange: String = "ECDHE with Curve X25519",
    val keySize: String = "256-bit ECC / 2048-bit RSA",
    val warnings: List<String> = emptyList()
)
