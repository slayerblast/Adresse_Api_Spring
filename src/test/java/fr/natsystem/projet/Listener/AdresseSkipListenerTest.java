package fr.natsystem.projet.Listener;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import fr.natsystem.projet.batch.listener.AdresseSkipListener;
import fr.natsystem.projet.model.Adresse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class AdresseSkipListenerTest {

  private AdresseSkipListener listener;

  @BeforeEach
  void setUp() {
    listener = new AdresseSkipListener();
  }

  @Test
  void shouldNotAddIdWhenSkippedDuringRead() {
    Throwable exception = new RuntimeException("Erreur pendant la lecture");

    listener.onSkipInRead(exception);

    assertTrue(listener.getSkippedIds().isEmpty());
    assertTrue(listener.getIdsRejetes().isEmpty());
  }

  @Test
  void shouldAddRejectedIdWhenSkippedDuringProcess() {
    Adresse adresse = mock(Adresse.class);

    when(adresse.id()).thenReturn("adresse-001");

    Throwable exception = new RuntimeException("Erreur pendant le traitement");

    listener.onSkipInProcess(adresse, exception);

    assertEquals(1, listener.getIdsRejetes().size());

    assertEquals("adresse-001", listener.getIdsRejetes().getFirst());

    assertTrue(listener.getSkippedIds().isEmpty());
  }

  @Test
  void shouldAddSkippedIdWhenSkippedDuringWrite() {
    Adresse adresse = mock(Adresse.class);

    when(adresse.id()).thenReturn("adresse-002");

    Throwable exception = new RuntimeException("Erreur pendant l'écriture");

    listener.onSkipInWrite(adresse, exception);

    assertEquals(1, listener.getSkippedIds().size());

    assertEquals("adresse-002", listener.getSkippedIds().getFirst());

    assertTrue(listener.getIdsRejetes().isEmpty());
  }

  @Test
  void shouldAccumulateRejectedIdsDuringProcess() {
    Adresse firstAddress = mock(Adresse.class);
    Adresse secondAddress = mock(Adresse.class);

    when(firstAddress.id()).thenReturn("adresse-001");
    when(secondAddress.id()).thenReturn("adresse-002");

    Throwable exception = new RuntimeException("Erreur pendant le traitement");

    listener.onSkipInProcess(firstAddress, exception);
    listener.onSkipInProcess(secondAddress, exception);

    assertEquals(2, listener.getIdsRejetes().size());

    assertEquals("adresse-001", listener.getIdsRejetes().get(0));

    assertEquals("adresse-002", listener.getIdsRejetes().get(1));

    assertTrue(listener.getSkippedIds().isEmpty());
  }

  @Test
  void shouldAccumulateSkippedIdsDuringWrite() {
    Adresse firstAddress = mock(Adresse.class);
    Adresse secondAddress = mock(Adresse.class);

    when(firstAddress.id()).thenReturn("adresse-003");
    when(secondAddress.id()).thenReturn("adresse-004");

    Throwable exception = new RuntimeException("Erreur pendant l'écriture");

    listener.onSkipInWrite(firstAddress, exception);
    listener.onSkipInWrite(secondAddress, exception);

    assertEquals(2, listener.getSkippedIds().size());

    assertEquals("adresse-003", listener.getSkippedIds().get(0));

    assertEquals("adresse-004", listener.getSkippedIds().get(1));

    assertTrue(listener.getIdsRejetes().isEmpty());
  }
}
