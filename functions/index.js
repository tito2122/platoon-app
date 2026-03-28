const {onRequest} = require('firebase-functions/v2/https');
const {defineSecret} = require('firebase-functions/params');

const anthropicKey = defineSecret('ANTHROPIC_API_KEY');

const PROMPT = 'אתה מנתח טבלת סידור יומי צבאי בעברית. משימתך: להבין לעומק את מבנה הטבלה ולחלץ את כל השיבוצים.\n\n--- הבנת מבנה הטבלה ---\n\nהטבלה מחולקת לשני אזורים עיקריים:\n\n1. עמדות שמירה (כותרות צהובות) — מאוישות לאורך כל היממה. לצידן עמודת זמן ראשית (כותרת שחורה) המשותפת לכולן, עם שעות: 8:00, 12:00, 16:00, 20:00, 0:00, 4:00. כל שורה בעמודת הזמן הראשית תואמת לשורה בכל עמדות השמירה.\n\n2. תפקידים נוספים (כותרות כחולות/סגולות) — מאוישים לשעה מסוימת בלבד. לכל אחד עמודת שעות ייעודית שחורה צמודה אליו משמאל, המציינת בדיוק באיזו שעה התא הרלוונטי מאויש. שאר התאים בעמודה ריקים.\n\n--- כללי חילוץ ---\n\n• פורמט שם בתא: "שם משפחה שם פרטי [מספר טלפון]" — הסר את מה שבסוגריים מרובעים []\n• אם תא מכיל מספר שמות — צור רשומה נפרדת לכל שם\n• התעלם מתאים ריקים לחלוטין\n• לכל שם: זהה את כותרת העמודה שלו (=role) ואת השעה מעמודת הזמן הרלוונטית באותה שורה (=time)\n• אל תניח שעות — קרא אותן תמיד מעמודת הזמן המתאימה\n\n--- פלט ---\n\nהחזר JSON תקני בלבד, ללא שום טקסט נוסף:\n{"assignments":[{"name":"שם מלא","role":"שם עמדה/תפקיד","time":"HH:MM"}]}';

exports.analyzeTask = onRequest(
  {secrets: [anthropicKey], cors: true, region: 'europe-west1'},
  async (req, res) => {
    if (req.method !== 'POST') {
      return res.status(405).json({error: 'Method Not Allowed'});
    }

    const {base64, mimeType} = req.body;
    if (!base64 || !mimeType) {
      return res.status(400).json({error: 'Missing base64 or mimeType'});
    }

    const apiKey = anthropicKey.value();

    const response = await fetch('https://api.anthropic.com/v1/messages', {
      method: 'POST',
      headers: {
        'x-api-key': apiKey,
        'anthropic-version': '2023-06-01',
        'content-type': 'application/json'
      },
      body: JSON.stringify({
        model: 'claude-opus-4-6',
        max_tokens: 4096,
        messages: [{
          role: 'user',
          content: [
            {type: 'image', source: {type: 'base64', media_type: mimeType, data: base64}},
            {type: 'text', text: PROMPT}
          ]
        }]
      })
    });

    const data = await response.json();
    if (!response.ok) {
      return res.status(response.status).json(data);
    }
    return res.json(data);
  }
);
