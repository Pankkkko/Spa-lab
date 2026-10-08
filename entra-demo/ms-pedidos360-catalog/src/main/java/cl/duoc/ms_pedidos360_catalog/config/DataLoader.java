package cl.duoc.ms_pedidos360_catalog.config;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import cl.duoc.ms_pedidos360_catalog.models.Producto;
import cl.duoc.ms_pedidos360_catalog.repository.ProductoRepository;

@Component
public class DataLoader implements CommandLineRunner {

    private final ProductoRepository repository;

    public DataLoader(ProductoRepository repository) {
        this.repository = repository;
    }

    @Override
    public void run(String... args) {

        // Evita duplicar los productos cada vez que inicia el micro
        if (repository.count() > 0) {
            return;
        }

        repository.saveAll(List.of(

            // =====================================================
            // FUSILES
            // =====================================================

            producto(
                "FUS-001",
                "Izhmash AKM",
                "Legendario Fusil de asalto sovietico, en calibre 7.62x39mm",
                "Fusiles",
                "189990",
                12
            ),

            producto(
                "FUS-002",
                "Colt M4A1",
                "Fusil de asalto basado en la plataforma AR15, calibre 5.56x45mm (tambien acepta .223 Remington)",
                "Fusiles",
                "219990",
                8
            ),

            producto(
                "FUS-003",
                "M4A1 SOPMOD",
                "Fusil de asalto con modificaciones SOPMOD, calibre 5.56x45mm.",
                "Fusiles",
                "249990",
                15
            ),

            producto(
                "FUS-004",
                "SIG SG 550",
                "Fusil de asalto suizo, calibre 5.56x45mm.",
                "Fusiles",
                "299990",
                6
            ),

            producto(
                "FUS-005",
                "H&K 416",
                "Fusil de asalto mejorado basado en la plataforma AR-15 con nuevo sistema de gas, calibre 5.56x45mm.",
                "Fusiles",
                "279990",
                10
            ),

            // =====================================================
            // ESCOPETAS
            // =====================================================

            producto(
                "ESC-001",
                "Remington 870",
                "Escopeta de accion de bombeo, calibre 12/70 numero 1 en agencias de policia y seguridad privada.",
                "Escopetas",
                "149990",
                9
            ),

            producto(
                "ESC-002",
                "Izhmash Saiga-12",
                "Escopeta semiautomatica de origen ruso basada en la plataforma Kalashnikov, calibre 12/70.",
                "Escopetas",
                "169990",
                7
            ),

            producto(
                "ESC-003",
                "Mossberg 500",
                "Escopeta de accion de bombeo, calibre 12/70, muy popular en el mercado civil y policial.",
                "Escopetas",
                "139990",
                14
            ),

            producto(
                "ESC-004",
                "Hatsan Escort",
                "Escopeta de accion de bombeo economica, calibre 12/70, ideal para principiantes y coleccionistas.",
                "Escopetas",
                "114990",
                5
            ),

            producto(
                "ESC-005",
                "Benelli M4 Super 90",
                "Escopeta de accion de bombeo, calibre 12/70, muy popular en el mercado civil y policial.",
                "Escopetas",
                "159990",
                11
            ),

            // =====================================================
            // CARABINAS
            // =====================================================

            producto(
                "CAR-001",
                "Ruger 10/22",
                "Carabina semi-automatica de calibre .22LR, ideal para tiro deportivo y recreativo.",
                "Carabinas",
                "129990",
                13
            ),

            producto(
                "CAR-002",
                "Izshmash AKS-74U",
                "Carabina compacta de origen ruso, version corta del AK-74, calibre 5.45x39mm.",
                "Carabinas",
                "154990",
                10
            ),

            producto(
                "CAR-003",
                "Smith & Wesson M&P15-22",
                "Carabina basada en la plataforma AR-15, calibre .22LR, ideal para entrenamiento y tiro deportivo.",
                "Carabinas",
                "179990",
                8
            ),

            producto(
                "CAR-004",
                "TOZ Simonov SKS",
                "Carabina de origen ruso, calibre 7.62x39mm, muy popular en el mercado civil y policial.",
                "Carabinas",
                "194990",
                6
            ),

            producto(
                "Molot VPO-209",
                "Orion Compact",
                "Carabina semi-automatica de origen ruso, basada en la plataforma Kalashnikov, calibre .366 TKM, ideal para cacería, tiro deportivo y recreativo.",
                "Carabinas",
                "164990",
                12
            ),

            // =====================================================
            // PISTOLAS
            // =====================================================

            producto(
                "PIS-001",
                "Glock 17 Gen 5",
                "Ergonomica pistola austriaca de calibre 9x19mm, ampliamente utilizada por fuerzas de seguridad y civiles.",
                "Pistolas",
                "89990",
                20
            ),

            producto(
                "PIS-002",
                "Glock 19 Gen 5",
                "Ergonomica pistola austriaca compacta de calibre 9x19mm, ampliamente utilizada por fuerzas de seguridad y civiles.",
                "Pistolas",
                "94990",
                16
            ),

            producto(
                "PIS-003",
                "Glock 21 Gen 5",
                "Ergonomica pistola austriaca de calibre .45 ACP, ampliamente utilizada por fuerzas de seguridad y civiles.",
                "Pistolas",
                "109990",
                18
            ),

            producto(
                "PIS-004",
                "CZ 75 Phantom",
                "Pistola Checa extremadamente precisa y confiable, de calibre 9x19mm.",
                "Pistolas",
                "84990",
                22
            ),

            producto(
                "PIS-005",
                "Beretta M9A3",
                "Pistola italiana de calibre 9x19mm, fue la pistola de servicio estandar del ejercito de los Estados Unidos durante varias decadas.",
                "Pistolas",
                "119990",
                9
            ),

            // =====================================================
            // REVOLVERES
            // =====================================================

            producto(
                "REV-001",
                "Smith & Wesson Model 10",
                "Clasico revolver de origen estadounidense, calibre .38 Special.",
                "Revolveres",
                "99990",
                7
            ),

            producto(
                "REV-002",
                "Smith & Wesson Model 686",
                "Moderno revolver ergonomico de origen estadounidense, calibre .357 Magnum.",
                "Revolveres",
                "124990",
                5
            ),

            producto(
                "REV-003",
                "Taurus Raging Bull",
                "Poderoso revolver de origen estadounidense, calibre .357 Magnum. ideal para defensa contra animales salvajes y caza mayor.",
                "Revolveres",
                "114990",
                8
            ),

            producto(
                "REV-004",
                "Colt Python",
                "Mitico revolver de culto de origen estadounidense, calibre .357 Magnum. muy apreciado por coleccionistas y entusiastas de armas de fuego.",
                "Revolveres",
                "104990",
                10
            ),

            producto(
                "REV-005",
                "Ruger Vaquero",
                "Revolver de estilo western de origen estadounidense, calibre .45 Colt. ideal para coleccionistas y entusiastas de armas de fuego.",
                "Revolveres",
                "129990",
                4
            ),

            // =====================================================
            // CARGADORES
            // =====================================================

            producto(
                "MAG-001",
                "Cargador STANAG 30",
                "Cargador de 30 rondas estandar para multitud de fusiles de la OTAN, acepta cartuchos de calibre 5.56x45mm, .223 Remington y .300 Blackout.",
                "Cargadores",
                "24990",
                30
            ),

            producto(
                "MAG-002",
                "Cargador de bakelita 30 para plataformas AK calibre 5.45x39mm.",
                "Cargador compatible con multitud de fusiles tipo AK, capacidad de 30 rondas, fabricado en bakelita y compatible con cartuchos de calibre 5.45x39mm.",
                "Cargadores",
                "29990",
                25
            ),

            producto(
                "MAG-003",
                "Cargador MAGPUL PMAG 30",
                "Cargador de 30 rondas fabricado por MAGPUL, compatible con multitud de fusiles estandar de la OTAN, acepta cartuchos de calibre 5.56x45mm, .223 Remington y .300 Blackout.",
                "Cargadores",
                "19990",
                35
            ),

            producto(
                "MAG-004",
                "Cargador de Glock 9x19mm de 17 rondas",
                "Cargador de 17 rondas compatible con pistolas Glock de calibre 9x19mm, fabricado en acero y polimero.",
                "Cargadores",
                "27990",
                18
            ),

            producto(
                "MAG-005",
                "Cargador de Glock .40 S&W de 15 rondas",
                "Cargador de 15 rondas compatible con pistolas Glock de calibre .40 S&W, fabricado en acero y polimero.",
                "Cargadores",
                "22990",
                27
            ),

            // =====================================================
            // MUNICION
            // =====================================================

            producto(
                "MUN-001",
                "Federal 9x19mm Luger HP 50rnd",
                "Caja de municion de 50 rondas para pistolas de calibre 9x19mm, fabricada por Federal con punta hueca.",
                "Municion",
                "8990",
                50
            ),

            producto(
                "MUN-002",
                "Fiocchi 9x19mm Luger FMJ 50rnd",
                "Caja de municion de 50 rondas para pistolas de calibre 9x19mm, fabricada por Fiocchi con punta de plomo encamisada en cobre.",
                "Municion",
                "11990",
                42
            ),

            producto(
                "MUN-003",
                "5.56x45mm NATO M855A1 50rnd",
                "Caja de municion de 50 rondas para fusiles de calibre 5.56x45mm, fabricada para uso militar, tiene capacidades perforantes mejoradas.",
                "Municion",
                "9990",
                60
            ),

            producto(
                "MUN-004",
                "5.56x45mm NATO M856A1 50rnd",
                "Caja de municion de 50 rondas para fusiles de calibre 5.56x45mm, fabricada para uso militar, tiene capacidades trazadoras y perforantes mejoradas.",
                "Municion",
                "12990",
                38
            ),

            producto(
                "MUN-005",
                "5.45x39mm 7N40 120rnd",
                "Caja de municion de 120 rondas para fusiles de calibre 5.45x39mm, fabricada para uso militar. con capacidades perforantes mejoradas",
                "Municion",
                "10990",
                45
            ),

            // =====================================================
            // ACCESORIOS
            // =====================================================

            producto(
                "ACC-001",
                "Trijicon ACOG 4x32",
                "Mirilla con aumento 4x32 de uso militar, fabricada por Trijicon, ideal para fusiles de asalto y carabinas.",
                "Accesorios",
                "44990",
                17
            ),

            producto(
                "ACC-002",
                "Trijicon SRS-02 mira reflex",
                "Mirilla de punto rojo fabricada por Trijicon, ideal para fusiles de asalto y carabinas.",
                "Accesorios",
                "29990",
                21
            ),

            producto(
                "ACC-003",
                "Aimpoint Micro T-2",
                "Mirilla de punto rojo compacta fabricada por Aimpoint, ideal para una gran variedad de armas de fuego",
                "Accesorios",
                "15990",
                28
            ),

            producto(
                "ACC-004",
                "ELCAN SpecterDR 1X/4X ",
                "Mirilla de aumento variable 1X/4X fabricada por ELCAN, ideal para fusiles de asalto y carabinas.",
                "Accesorios",
                "54990",
                13
            ),

            producto(
                "ACC-005",
                "EOTech EXPS2-0",
                "Mirilla holografica fabricada por EOTech, ideal para fusiles de asalto y carabinas.",
                "Accesorios",
                "24990",
                24
            )
        ));

        System.out.println(
            "=============================================="
        );

        System.out.println(
            "DataLoader: 40 productos cargados correctamente."
        );

        System.out.println(
            "=============================================="
        );
    }

    private Producto producto(
            String sku,
            String nombre,
            String descripcion,
            String categoria,
            String precio,
            Integer stock
    ) {

        return Producto.builder()
                .sku(sku)
                .nombre(nombre)
                .descripcion(descripcion)
                .categoria(categoria)
                .precio(new BigDecimal(precio))
                .imagenes(
                    List.of(
                        "https://placehold.co/600x600?text="
                            + sku
                    )
                )
                .stock(stock)
                .build();
    }
}