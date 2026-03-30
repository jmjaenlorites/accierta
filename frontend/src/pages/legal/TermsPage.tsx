import { Link } from 'react-router-dom'
import { ArrowLeft } from 'lucide-react'
import { useTranslation } from 'react-i18next'

const content = {
  'es-ES': {
    title: 'Términos de Servicio',
    lastUpdated: 'Última actualización: marzo 2026',
    sections: [
      {
        heading: '1. Aceptación de los términos',
        body: 'Al acceder o utilizar Accierta ("el Servicio"), aceptas quedar vinculado por estos Términos de Servicio. Si no estás de acuerdo con alguna parte de estos términos, no debes utilizar el Servicio. El uso continuado del Servicio después de cualquier modificación constituye la aceptación de dichos cambios.',
      },
      {
        heading: '2. Descripción del Servicio',
        body: 'Accierta es una plataforma de entretenimiento social que permite a grupos de amigos crear predicciones sobre eventos y apostar **puntos ficticios** entre ellos. El Servicio:\n\n• **NO es una plataforma de apuestas con dinero real.** No se gestiona, procesa ni facilita ninguna transacción monetaria.\n• **NO es un servicio financiero** de ningún tipo.\n• **NO constituye juego de azar** según la normativa aplicable, dado que no hay contraprestación económica.\n• Es una herramienta de entretenimiento social similar a un marcador o tabla de clasificación entre amigos.',
      },
      {
        heading: '3. Exención total de responsabilidad sobre dinero real',
        body: '**Accierta no gestiona, facilita, intermedia ni tiene relación alguna con transacciones de dinero real entre usuarios.** Si los usuarios de un grupo deciden voluntariamente gestionar dinero real entre ellos fuera de la plataforma, lo hacen bajo su exclusiva responsabilidad.\n\nEl responsable del Servicio:\n\n• **No es responsable** de ningún acuerdo económico, deuda, pago o transacción entre usuarios.\n• **No es responsable** de pérdidas económicas derivadas del uso de la app como referencia para apuestas con dinero real.\n• **No actúa como intermediario** financiero de ningún tipo.\n• **No tiene obligación** de resolver disputas económicas entre usuarios.\n\nLas cifras mostradas en la aplicación (deudas, balances, clasificaciones) son meramente informativas y se refieren a la moneda ficticia elegida por cada grupo. **No representan deudas reales ni obligaciones legalmente exigibles.**',
      },
      {
        heading: '4. Cuentas de usuario',
        body: 'Para usar el Servicio necesitas crear una cuenta con un email válido y una contraseña. Eres responsable de:\n\n• Mantener la confidencialidad de tus credenciales.\n• Todas las actividades que ocurran bajo tu cuenta.\n• Notificarnos inmediatamente si sospechas de un uso no autorizado.\n\nNos reservamos el derecho de suspender o eliminar cuentas que violen estos términos.',
      },
      {
        heading: '5. Conducta del usuario',
        body: 'Al usar el Servicio, te comprometes a:\n\n• No utilizar la plataforma para actividades ilegales.\n• No crear predicciones con contenido ofensivo, discriminatorio, violento o que incite al odio.\n• No intentar acceder a cuentas o datos de otros usuarios.\n• No interferir con el funcionamiento técnico del Servicio.\n• No utilizar la plataforma para facilitar apuestas ilegales o actividades de juego no autorizadas.\n• Respetar a los demás miembros de tus grupos.',
      },
      {
        heading: '6. Contenido generado por usuarios',
        body: 'Los usuarios son los únicos responsables del contenido que crean (predicciones, nombres de grupo, apuestas, propuestas de resultado). El Servicio no modera ni revisa el contenido de forma proactiva.\n\n**No nos hacemos responsables** del contenido creado por los usuarios. Nos reservamos el derecho de eliminar contenido que viole estos términos si somos notificados.',
      },
      {
        heading: '7. Resolución de disputas dentro de la app',
        body: 'El mecanismo de resolución de predicciones se basa en el consenso del grupo mediante votación. **El Servicio no es árbitro ni mediador** en las disputas entre usuarios. Los resultados de las votaciones son definitivos dentro de la plataforma.\n\nSi un grupo no puede alcanzar un consenso, los usuarios deben resolver la situación entre ellos. El Servicio no interviene en disputas entre miembros de un grupo.',
      },
      {
        heading: '8. Disponibilidad del Servicio',
        body: 'El Servicio se proporciona **"tal cual" (as is) y "según disponibilidad" (as available).**\n\n• No garantizamos que el Servicio esté disponible de forma ininterrumpida o libre de errores.\n• Nos reservamos el derecho de modificar, suspender o discontinuar el Servicio en cualquier momento, con o sin previo aviso.\n• No somos responsables de la pérdida de datos derivada de interrupciones del servicio, fallos técnicos o mantenimiento.',
      },
      {
        heading: '9. Limitación de responsabilidad',
        body: '**En la máxima medida permitida por la ley aplicable:**\n\n• El Servicio se ofrece sin garantías de ningún tipo, ya sean expresas o implícitas, incluyendo pero no limitándose a garantías de comerciabilidad, idoneidad para un fin particular o no infracción.\n• En ningún caso el responsable del Servicio será responsable de daños directos, indirectos, incidentales, especiales, consecuentes o punitivos, incluyendo pero no limitándose a pérdida de beneficios, datos, uso o cualquier otra pérdida intangible.\n• La responsabilidad total del Servicio frente al usuario, por cualquier causa y bajo cualquier teoría de responsabilidad, **no excederá en ningún caso la cantidad de cero euros (0 €)**, dado que el Servicio es completamente gratuito.\n• Esta limitación de responsabilidad se aplica independientemente de que el responsable del Servicio haya sido advertido de la posibilidad de dichos daños.',
      },
      {
        heading: '10. Indemnización',
        body: 'Aceptas indemnizar y mantener indemne al responsable del Servicio frente a cualquier reclamación, daño, pérdida, responsabilidad, coste o gasto (incluyendo honorarios legales razonables) que surja de:\n\n• Tu uso del Servicio.\n• Tu violación de estos Términos.\n• Tu violación de los derechos de terceros.\n• Cualquier acuerdo económico que alcances con otros usuarios fuera de la plataforma.',
      },
      {
        heading: '11. Propiedad intelectual',
        body: 'El código fuente, diseño, marca y contenido original de Accierta son propiedad del responsable del Servicio. Los usuarios conservan los derechos sobre el contenido que crean, pero otorgan una licencia gratuita y no exclusiva para que el Servicio pueda mostrarlo y procesarlo.',
      },
      {
        heading: '12. Edad mínima',
        body: 'Debes tener al menos 16 años para utilizar el Servicio. Al registrarte, declaras que cumples con este requisito.',
      },
      {
        heading: '13. Modificaciones',
        body: 'Nos reservamos el derecho de modificar estos Términos en cualquier momento. Los cambios significativos se notificarán a los usuarios registrados por email. El uso continuado del Servicio tras la notificación constituye la aceptación de los nuevos términos.',
      },
      {
        heading: '14. Ley aplicable y jurisdicción',
        body: 'Estos Términos se rigen por la legislación española. Para cualquier controversia derivada de estos Términos o del uso del Servicio, las partes se someten a los juzgados y tribunales competentes.',
      },
      {
        heading: '15. Contacto',
        body: 'Para cualquier consulta sobre estos Términos de Servicio, puedes contactarnos en legal@accierta.com.',
      },
    ],
  },
  'en-US': {
    title: 'Terms of Service',
    lastUpdated: 'Last updated: March 2026',
    sections: [
      {
        heading: '1. Acceptance of Terms',
        body: 'By accessing or using Accierta ("the Service"), you agree to be bound by these Terms of Service. If you do not agree with any part of these terms, you must not use the Service. Continued use of the Service after any modifications constitutes acceptance of those changes.',
      },
      {
        heading: '2. Description of the Service',
        body: 'Accierta is a social entertainment platform that allows groups of friends to create predictions about events and bet **fictional points** among themselves. The Service:\n\n• **Is NOT a real-money betting platform.** No monetary transaction is managed, processed, or facilitated.\n• **Is NOT a financial service** of any kind.\n• **Does NOT constitute gambling** under applicable regulations, as there is no monetary consideration.\n• Is a social entertainment tool similar to a scoreboard or leaderboard among friends.',
      },
      {
        heading: '3. Complete Disclaimer of Liability for Real Money',
        body: '**Accierta does not manage, facilitate, intermediate, or have any relationship with real-money transactions between users.** If users in a group voluntarily decide to manage real money among themselves outside the platform, they do so at their sole responsibility.\n\nThe Service operator:\n\n• **Is not responsible** for any economic agreement, debt, payment, or transaction between users.\n• **Is not responsible** for financial losses derived from using the app as a reference for real-money bets.\n• **Does not act as a financial intermediary** of any kind.\n• **Has no obligation** to resolve economic disputes between users.\n\nFigures shown in the application (debts, balances, rankings) are purely informational and refer to the fictional currency chosen by each group. **They do not represent real debts or legally enforceable obligations.**',
      },
      {
        heading: '4. User Accounts',
        body: 'To use the Service you need to create an account with a valid email and password. You are responsible for:\n\n• Maintaining the confidentiality of your credentials.\n• All activities that occur under your account.\n• Notifying us immediately if you suspect unauthorized use.\n\nWe reserve the right to suspend or delete accounts that violate these terms.',
      },
      {
        heading: '5. User Conduct',
        body: 'By using the Service, you agree to:\n\n• Not use the platform for illegal activities.\n• Not create predictions with offensive, discriminatory, violent, or hate-inciting content.\n• Not attempt to access other users\' accounts or data.\n• Not interfere with the technical operation of the Service.\n• Not use the platform to facilitate illegal betting or unauthorized gambling activities.\n• Respect other members of your groups.',
      },
      {
        heading: '6. User-Generated Content',
        body: 'Users are solely responsible for the content they create (predictions, group names, bets, result proposals). The Service does not proactively moderate or review content.\n\n**We are not responsible** for user-generated content. We reserve the right to remove content that violates these terms if notified.',
      },
      {
        heading: '7. In-App Dispute Resolution',
        body: 'The prediction resolution mechanism is based on group consensus through voting. **The Service is not an arbitrator or mediator** in disputes between users. Voting results are final within the platform.\n\nIf a group cannot reach consensus, users must resolve the situation among themselves. The Service does not intervene in disputes between group members.',
      },
      {
        heading: '8. Service Availability',
        body: 'The Service is provided **"as is" and "as available."**\n\n• We do not guarantee that the Service will be available uninterrupted or error-free.\n• We reserve the right to modify, suspend, or discontinue the Service at any time, with or without notice.\n• We are not responsible for data loss resulting from service interruptions, technical failures, or maintenance.',
      },
      {
        heading: '9. Limitation of Liability',
        body: '**To the maximum extent permitted by applicable law:**\n\n• The Service is offered without warranties of any kind, whether express or implied, including but not limited to warranties of merchantability, fitness for a particular purpose, or non-infringement.\n• In no event shall the Service operator be liable for direct, indirect, incidental, special, consequential, or punitive damages, including but not limited to loss of profits, data, use, or any other intangible loss.\n• The total liability of the Service to the user, for any cause and under any theory of liability, **shall in no case exceed zero euros (€0)**, as the Service is completely free.\n• This limitation of liability applies regardless of whether the Service operator has been advised of the possibility of such damages.',
      },
      {
        heading: '10. Indemnification',
        body: 'You agree to indemnify and hold harmless the Service operator from any claims, damages, losses, liabilities, costs, or expenses (including reasonable legal fees) arising from:\n\n• Your use of the Service.\n• Your violation of these Terms.\n• Your violation of third-party rights.\n• Any economic agreement you reach with other users outside the platform.',
      },
      {
        heading: '11. Intellectual Property',
        body: 'The source code, design, brand, and original content of Accierta are the property of the Service operator. Users retain rights to the content they create, but grant a free, non-exclusive license for the Service to display and process it.',
      },
      {
        heading: '12. Minimum Age',
        body: 'You must be at least 16 years old to use the Service. By registering, you declare that you meet this requirement.',
      },
      {
        heading: '13. Modifications',
        body: 'We reserve the right to modify these Terms at any time. Significant changes will be notified to registered users by email. Continued use of the Service after notification constitutes acceptance of the new terms.',
      },
      {
        heading: '14. Governing Law and Jurisdiction',
        body: 'These Terms are governed by Spanish law. For any dispute arising from these Terms or the use of the Service, the parties submit to the competent courts.',
      },
      {
        heading: '15. Contact',
        body: 'For any inquiries about these Terms of Service, you can contact us at legal@accierta.com.',
      },
    ],
  },
}

export function TermsPage() {
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
