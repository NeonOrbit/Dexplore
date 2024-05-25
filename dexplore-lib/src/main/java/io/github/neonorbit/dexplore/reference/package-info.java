/**
 * References to constants found in dex instructions.
 * <p>
 * <b>Note:</b> These references consist primarily of string literals
 * and identifiers, such as the names of types, fields and methods, which are used within dex classes.
 * <p>
 * Types of References:
 * <ul>
 *   <li>
 *     <b>{@linkplain io.github.neonorbit.dexplore.reference.StringRefData String Reference:}</b>
 *     Reference to a string literal.
 *   </li>
 *   <li>
 *     <b>{@linkplain io.github.neonorbit.dexplore.reference.TypeRefData Type Reference:}</b>
 *     Reference to a type identifier (eg: full name of a class).
 *   </li>
 *   <li>
 *     <b>{@linkplain io.github.neonorbit.dexplore.reference.FieldRefData Field Reference:}</b>
 *     Reference to a field identifier (signature of a field).
 *   </li>
 *   <li>
 *     <b>{@linkplain io.github.neonorbit.dexplore.reference.MethodRefData Method Reference:}</b>
 *     Reference to a method identifier (method signature).
 *   </li>
 * </ul>
 * <p>
 * Dexplore creates a {@linkplain io.github.neonorbit.dexplore.ReferencePool ReferencePool} object
 * for each dex class, field and method found in dex files.
 * Each of these pools contains the references found within the instructions of their respective dex items.
 * <p>
 * Java Sample:
 * <pre>{@code
 *   public class Sample {
 *     private Context context;
 *
 *     // String Reference: "AppInit"
 *     static final String TAG = "AppInit";
 *
 *     public void init(Activity activity) {
 *       // Type Reference: Context
 *       context = (Context) activity;
 *
 *       // Field Reference: SDK_INT
 *       if (Build.VERSION.SDK_INT >= 30) {
 *
 *         // Method Reference: getMessage()
 *         String msg = getMessage();
 *
 *         // Method Reference: logMessage()
 *         Util.logMessage(msg);
 *       }
 *     }
 *   }
 * }</pre>
 * <p>
 * <b>Note:</b> The ReferencePool of a class does not include references to its own declared fields and methods,
 * except when they are used internally within the class.
 * <p>
 * Refer to the <a href="https://github.com/NeonOrbit/Dexplore/wiki">Dexplore Wiki </a> for detailed explanation and examples.
 * <p>
 *   {@code NeonOrbit}
 * <br>
 */
package io.github.neonorbit.dexplore.reference;
