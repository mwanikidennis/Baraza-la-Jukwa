import { FastifyInstance } from 'fastify';
import { GoogleGenerativeAI } from '@google/generative-ai';

export default async function (fastify: FastifyInstance) {
  const genAI = new GoogleGenerativeAI(process.env.GEMINI_API_KEY || '');
  const model = genAI.getGenerativeModel({ model: "gemini-pro" });

  // Incident Triage with "Thought Signature" Loop
  fastify.post('/triage', async (request, reply) => {
    const { incident_text, media_desc, previous_thoughts } = request.body as any;

    const prompt = `
      System: You are the Jukwa AI Orchestrator. 
      Context: ${previous_thoughts || 'New session'}
      Incident: ${incident_text}
      Media Description: ${media_desc}
      
      Task: Triage this incident. Provide severity (1-5), primary agency, and recommended immediate action.
      Return JSON format.
    `;

    try {
      const result = await model.generateContent(prompt);
      const response = await result.response;
      const text = response.text();
      
      // Simulating "Thought Signature" by passing back the model's reasoning/state
      // In a real implementation with Gemini 3.1 Pro, this would be a specific metadata field
      const thought_signature = Buffer.from(text).toString('base64').substring(0, 100); 

      return {
        analysis: text,
        thought_signature,
        status: 'CLASSIFIED'
      };
    } catch (err) {
      fastify.log.error(err);
      return reply.status(500).send({ error: 'AI Triage failed' });
    }
  });

  // Generative UI Layout Request
  fastify.post('/ui/generate', async (request, reply) => {
    const { user_state, context } = request.body as any;
    
    // Logic to return a JSON layout descriptor for the Android SDUI engine
    return {
      layout_id: 'emergency_sos',
      components: [
        { type: 'BANNER', content: 'EMERGENCY DETECTED', color: 'RED' },
        { type: 'BUTTON', action: 'SOS_CALL', label: 'Call 999' },
        { type: 'STREAM', source: 'OB_LIVE' }
      ],
      tier_constraints: { min_ram_gb: 2 }
    };
  });
}
