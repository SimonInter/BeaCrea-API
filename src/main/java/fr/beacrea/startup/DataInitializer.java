package fr.beacrea.startup;

import fr.beacrea.entity.AppUser;
import io.quarkus.logging.Log;
import io.quarkus.runtime.StartupEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.transaction.Transactional;
import org.mindrot.jbcrypt.BCrypt;

import java.time.Instant;

/**
 * Initialise les données obligatoires au démarrage de l'application.
 * Crée le compte admin si inexistant (avec mot de passe haché).
 */
@ApplicationScoped
public class DataInitializer {

    @Transactional
    public void onStart(@Observes StartupEvent pEvent) {
        createAdminIfAbsent();
    }

    private void createAdminIfAbsent() {
        if (AppUser.findByEmail("admin@beacrea.fr") != null) {
            return;
        }

        AppUser lAdmin = new AppUser();
        lAdmin.id = "admin_1";
        lAdmin.email = "admin@beacrea.fr";
        // Mot de passe par défaut : "admin123" — à changer immédiatement en production !
        lAdmin.password = BCrypt.hashpw("admin123", BCrypt.gensalt(10));
        lAdmin.firstName = "Admin";
        lAdmin.lastName = "BeaCrea";
        lAdmin.role = "admin";
        lAdmin.createdAt = Instant.now().toString();
        lAdmin.persist();

        Log.info("Admin account created. Email: admin@beacrea.fr — CHANGE PASSWORD IN PRODUCTION!");
    }
}
