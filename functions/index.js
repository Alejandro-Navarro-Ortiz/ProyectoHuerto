const functions = require('firebase-functions/v1');
const admin = require('firebase-admin');

admin.initializeApp();

/**
 * Trigger que se dispara cuando se crea un nuevo bancal
 * o se añade un documento a la colección de bancales.
 *
 * NOTA: Para probar "al plantar", detectaremos la creación del documento.
 */
exports.notificarNuevaSiembra = functions.firestore
    .document('usuarios/{userId}/bancales/{bancalId}')
    .onCreate(async (snapshot, context) => {
        const userId = context.params.userId;
        const nuevoBancal = snapshot.data();

        try {
            // 1. Obtener el token FCM del usuario desde su perfil
            const userDoc = await admin.firestore().collection('usuarios').doc(userId).get();
            const fcmToken = userDoc.data() ? userDoc.data().fcmToken : null;

            if (!fcmToken) {
                console.log(`Usuario ${userId} no tiene token FCM registrado.`);
                return null;
            }

            // 2. Construir el mensaje de notificación
            const message = {
                notification: {
                    title: '🌱 ¡Nueva planta detectada!',
                    body: `Has creado el bancal "${nuevoBancal.nombre}". ¡No olvides regar tus nuevos cultivos!`
                },
                token: fcmToken
            };

            // 3. Enviar la notificación a través de Firebase Cloud Messaging
            const response = await admin.messaging().send(message);
            console.log('Notificación enviada con éxito:', response);
            return response;

        } catch (error) {
            console.error('Error enviando notificación automática:', error);
            return null;
        }
    });

/**
 * Función para pruebas de inactividad o riego programado (opcional)
 */
exports.verificarRiegoManual = functions.https.onRequest(async (req, res) => {
    // Esta función permite disparar una notificación a todos simplemente
    // entrando en una URL que te dará Firebase, útil para pruebas rápidas.
    const querySnapshot = await admin.firestore().collection('usuarios').get();

    const promises = [];
    querySnapshot.forEach(doc => {
        const token = doc.data().fcmToken;
        if (token) {
            const message = {
                notification: {
                    title: 'Prueba de Sistema',
                    body: 'El servidor de HuertoApp está operativo.'
                },
                token: token
            };
            promises.push(admin.messaging().send(message));
        }
    });

    await Promise.all(promises);
    res.send("Notificaciones de prueba enviadas.");
});