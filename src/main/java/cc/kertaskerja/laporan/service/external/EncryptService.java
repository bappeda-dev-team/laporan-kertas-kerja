package cc.kertaskerja.laporan.service.external;

import org.springframework.stereotype.Service;

@Service
public interface EncryptService {
    String encrypt(String plainText);

    String decrypt(String encryptedText);
}
