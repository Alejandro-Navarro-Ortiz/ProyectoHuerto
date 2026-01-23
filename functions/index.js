const functions = require('firebase-functions/v1');
const admin = require('firebase-admin');

admin.initializeApp();

/**
 * Trigger que se dispara al crear un bancal.
 * Espera 20 segundos y envía la notificación de riego para pruebas.
 */
exports.notificarNuevaSiembraProgramada = functions.firestore
    .document('usuarios/{userId}/bancales/{bancalId}')
    .onCreate(async (snapshot, context) => {
        const userId = context.params.userId;
        const nuevoBancal = snapshot.data();

        console.log(`Siembra detectada para usuario ${userId}. Esperando 20 segundos...`);

        // Simulación de retraso para pruebas
        const delay = (ms) => new Promise(resolve => setTimeout(resolve, ms));
        await delay(20000);

        try {
            const userDoc = await admin.firestore().collection('usuarios').doc(userId).get();
            const fcmToken = userDoc.data() ? userDoc.data().fcmToken : null;

            if (!fcmToken) {
                console.log(`Usuario ${userId} sin token FCM.`);
                return null;
            }

            const message = {
                notification: {
                    title: '💧 ¡Hora de regar!',
                    body: `Han pasado 20 segundos desde que plantaste en "${nuevoBancal.nombre}".`
                },
                token: fcmToken,
                android: {
                    priority: 'high'
                }
            };

            const response = await admin.messaging().send(message);
            console.log('Notificación enviada:', response);
            return response;

        } catch (error) {
            console.error('Error en notificación:', error);
            return null;
        }
    });

exports.verificarRiegoManual = functions.https.onRequest(async (req, res) => {
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
    res.send("Notificaciones enviadas.");
});