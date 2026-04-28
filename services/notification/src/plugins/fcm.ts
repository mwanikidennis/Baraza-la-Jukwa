import fp from 'fastify-plugin';

export default fp(async (fastify) => {
  // FCM Initialization logic
  const sendPush = async (token: string, title: string, body: string, data: any) => {
    fastify.log.info(`Push notification sent to ${token}: ${title}`);
    // Real implementation: admin.messaging().send(...)
    return { success: true, messageId: 'fcm_mock_id' };
  };

  fastify.decorate('fcm', { sendPush });
});

declare module 'fastify' {
  export interface FastifyInstance {
    fcm: {
      sendPush: (token: string, title: string, body: string, data: any) => Promise<any>;
    };
  }
}
