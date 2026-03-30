import { Link } from 'react-router-dom'
import { ArrowLeft } from 'lucide-react'
import { useTranslation } from 'react-i18next'

const content = {
  'es-ES': {
    title: 'Política de Privacidad',
    lastUpdated: 'Última actualización: marzo 2026',
    sections: [
      {
        heading: '1. Responsable del tratamiento',
        body: 'Accierta es un proyecto personal sin entidad jurídica constituida. El responsable del tratamiento de datos es el desarrollador del servicio. Si tienes preguntas sobre tus datos, puedes contactarnos en privacy@accierta.com.',
      },
      {
        heading: '2. Datos que recopilamos',
        body: 'Recopilamos exclusivamente los datos necesarios para el funcionamiento del servicio:\n\n• **Email**: para registro, inicio de sesión, verificación de cuenta y recuperación de contraseña.\n• **Contraseña**: almacenada de forma cifrada (hash + salt). Nunca se almacena en texto plano.\n• **Datos de uso**: predicciones creadas, apuestas realizadas, votos emitidos y participación en grupos. Estos datos son necesarios para el funcionamiento de la app.\n• **Identificadores técnicos**: tokens de sesión (JWT) almacenados en el navegador del usuario.',
      },
      {
        heading: '3. Datos que NO recopilamos',
        body: '• No recopilamos datos financieros ni bancarios.\n• No recopilamos datos de localización.\n• No utilizamos cookies de seguimiento ni de terceros.\n• No recopilamos datos biométricos.\n• No realizamos perfilado con fines publicitarios.',
      },
      {
        heading: '4. Finalidad del tratamiento',
        body: 'Tus datos se utilizan exclusivamente para:\n\n• Crear y mantener tu cuenta de usuario.\n• Permitir la participación en grupos y predicciones.\n• Enviar emails transaccionales (verificación, recuperación de contraseña, invitaciones a grupos).\n• Mantener el funcionamiento técnico del servicio.',
      },
      {
        heading: '5. Base legal',
        body: 'El tratamiento se basa en el consentimiento del usuario al registrarse y aceptar los Términos de Servicio, así como en el interés legítimo de mantener el servicio operativo y seguro.',
      },
      {
        heading: '6. Comunicación de datos a terceros',
        body: '• **Resend**: servicio de envío de emails transaccionales. Recibe tu dirección de email exclusivamente para el envío de correos del servicio.\n• **No vendemos, cedemos ni compartimos tus datos** con terceros para fines comerciales, publicitarios ni de ningún otro tipo.',
      },
      {
        heading: '7. Conservación de datos',
        body: 'Tus datos se conservan mientras mantengas tu cuenta activa. Si solicitas la eliminación de tu cuenta, eliminaremos tus datos personales en un plazo razonable, salvo que exista una obligación legal que requiera su conservación.',
      },
      {
        heading: '8. Derechos del usuario',
        body: 'Puedes ejercer los siguientes derechos contactando a privacy@accierta.com:\n\n• **Acceso**: solicitar una copia de tus datos personales.\n• **Rectificación**: corregir datos inexactos.\n• **Supresión**: solicitar la eliminación de tu cuenta y datos.\n• **Portabilidad**: recibir tus datos en un formato estándar.\n• **Oposición**: oponerte al tratamiento de tus datos.',
      },
      {
        heading: '9. Seguridad',
        body: 'Implementamos medidas técnicas razonables para proteger tus datos: cifrado de contraseñas, comunicaciones HTTPS, tokens de sesión con expiración, y control de acceso basado en roles. Sin embargo, ningún sistema es 100% seguro y no podemos garantizar la seguridad absoluta.',
      },
      {
        heading: '10. Menores de edad',
        body: 'Accierta no está dirigido a menores de 16 años. No recopilamos conscientemente datos de menores. Si detectamos que un menor se ha registrado, eliminaremos su cuenta.',
      },
      {
        heading: '11. Cambios en esta política',
        body: 'Nos reservamos el derecho de modificar esta política. Los cambios significativos se comunicarán a los usuarios registrados por email. El uso continuado del servicio tras la notificación implica la aceptación de los cambios.',
      },
    ],
  },
  'en-US': {
    title: 'Privacy Policy',
    lastUpdated: 'Last updated: March 2026',
    sections: [
      {
        heading: '1. Data Controller',
        body: 'Accierta is a personal project without a constituted legal entity. The data controller is the developer of the service. If you have questions about your data, you can contact us at privacy@accierta.com.',
      },
      {
        heading: '2. Data We Collect',
        body: 'We collect only the data necessary for the service to function:\n\n• **Email**: for registration, login, account verification, and password recovery.\n• **Password**: stored in encrypted form (hash + salt). Never stored in plain text.\n• **Usage data**: predictions created, bets placed, votes cast, and group participation. This data is necessary for the app to function.\n• **Technical identifiers**: session tokens (JWT) stored in the user\'s browser.',
      },
      {
        heading: '3. Data We Do NOT Collect',
        body: '• We do not collect financial or banking data.\n• We do not collect location data.\n• We do not use tracking or third-party cookies.\n• We do not collect biometric data.\n• We do not profile users for advertising purposes.',
      },
      {
        heading: '4. Purpose of Data Processing',
        body: 'Your data is used exclusively to:\n\n• Create and maintain your user account.\n• Enable participation in groups and predictions.\n• Send transactional emails (verification, password recovery, group invitations).\n• Maintain the technical operation of the service.',
      },
      {
        heading: '5. Legal Basis',
        body: 'Data processing is based on user consent upon registration and acceptance of the Terms of Service, as well as the legitimate interest of maintaining the service operational and secure.',
      },
      {
        heading: '6. Third-Party Data Sharing',
        body: '• **Resend**: transactional email service. Receives your email address exclusively for sending service emails.\n• **We do not sell, transfer, or share your data** with third parties for commercial, advertising, or any other purpose.',
      },
      {
        heading: '7. Data Retention',
        body: 'Your data is retained as long as your account remains active. If you request account deletion, we will delete your personal data within a reasonable timeframe, unless a legal obligation requires its retention.',
      },
      {
        heading: '8. User Rights',
        body: 'You can exercise the following rights by contacting privacy@accierta.com:\n\n• **Access**: request a copy of your personal data.\n• **Rectification**: correct inaccurate data.\n• **Erasure**: request deletion of your account and data.\n• **Portability**: receive your data in a standard format.\n• **Objection**: object to the processing of your data.',
      },
      {
        heading: '9. Security',
        body: 'We implement reasonable technical measures to protect your data: password encryption, HTTPS communications, session tokens with expiration, and role-based access control. However, no system is 100% secure and we cannot guarantee absolute security.',
      },
      {
        heading: '10. Minors',
        body: 'Accierta is not intended for users under 16 years of age. We do not knowingly collect data from minors. If we detect that a minor has registered, we will delete their account.',
      },
      {
        heading: '11. Changes to This Policy',
        body: 'We reserve the right to modify this policy. Significant changes will be communicated to registered users by email. Continued use of the service after notification implies acceptance of the changes.',
      },
    ],
  },
}

export function PrivacyPage() {
  const { i18n } = useTranslation()
  const lang = i18n.language === 'en-US' ? 'en-US' : 'es-ES'
  const c = content[lang]

  return (
    <div className="legal-page">
      <div className="legal-container">
        <Link to="/landing" className="legal-back">
          <ArrowLeft size={18} />
          Accierta
        </Link>

        <h1 className="legal-title">{c.title}</h1>
        <p className="legal-updated">{c.lastUpdated}</p>

        {c.sections.map((section, i) => (
          <div className="legal-section" key={i}>
            <h2 className="legal-heading">{section.heading}</h2>
            <div
              className="legal-body"
              dangerouslySetInnerHTML={{
                __html: section.body
                  .replace(/\n/g, '<br/>')
                  .replace(/\*\*(.*?)\*\*/g, '<strong>$1</strong>'),
              }}
            />
          </div>
        ))}
      </div>
    </div>
  )
}
