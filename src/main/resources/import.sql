-- ============================================================
-- Categories
-- ============================================================
INSERT INTO categories (id, name, icon, count) VALUES ('robes', 'Robes', '👗', 2) ON CONFLICT (id) DO NOTHING;
INSERT INTO categories (id, name, icon, count) VALUES ('tops', 'Tops & Chemises', '👕', 3) ON CONFLICT (id) DO NOTHING;
INSERT INTO categories (id, name, icon, count) VALUES ('pantalons', 'Pantalons', '👖', 1) ON CONFLICT (id) DO NOTHING;
INSERT INTO categories (id, name, icon, count) VALUES ('jupes', 'Jupes', '🩱', 1) ON CONFLICT (id) DO NOTHING;
INSERT INTO categories (id, name, icon, count) VALUES ('vestes', 'Vestes', '🧥', 1) ON CONFLICT (id) DO NOTHING;
INSERT INTO categories (id, name, icon, count) VALUES ('maison', 'Maison', '🏡', 2) ON CONFLICT (id) DO NOTHING;
INSERT INTO categories (id, name, icon, count) VALUES ('cuisine', 'Cuisine', '🍳', 1) ON CONFLICT (id) DO NOTHING;
INSERT INTO categories (id, name, icon, count) VALUES ('accessoires', 'Accessoires', '👜', 2) ON CONFLICT (id) DO NOTHING;

-- ============================================================
-- Products
-- ============================================================

-- 1 - Robe Lin Sauvage
INSERT INTO products (id, name, slug, category, price, original_price, description, long_description, images, sizes, colors, stock, rating, review_count, tags, featured, is_new, badge)
VALUES (
    1, 'Robe Lin Sauvage', 'robe-lin-sauvage', 'robes', 89.00, NULL,
    'Une robe légère en lin naturel, teinture végétale. Coupe fluide et intemporelle, parfaite pour toutes les saisons.',
    'Confectionnée en lin 100% naturel, cette robe incarne l''élégance décontractée. Sa coupe légèrement évasée flatte toutes les silhouettes. La teinture végétale lui confère des nuances uniques et douces. Poche latérale discrète. Entretien facile : lavage à 30°.',
    '["https://images.unsplash.com/photo-1594938298603-c8148c4b4459?w=600&q=80","https://images.unsplash.com/photo-1515886657613-9f3515b0c78f?w=600&q=80","https://images.unsplash.com/photo-1496747611176-843222e1e57c?w=600&q=80"]',
    '["XS","S","M","L","XL"]',
    '[{"name":"Écru","hex":"#f5f0e8"},{"name":"Terracotta","hex":"#c26b4e"},{"name":"Sauge","hex":"#8da68a"}]',
    '{"XS":3,"S":8,"M":12,"L":6,"XL":2}',
    4.8, 24, '["lin","naturel","éco-responsable"]', true, true, 'Nouveau'
) ON CONFLICT (id) DO NOTHING;

-- 2 - Top Brodé Floral
INSERT INTO products (id, name, slug, category, price, original_price, description, long_description, images, sizes, colors, stock, rating, review_count, tags, featured, is_new, badge)
VALUES (
    2, 'Top Brodé Floral', 'top-brode-floral', 'tops', 52.00, 68.00,
    'Top en coton biologique avec broderies florales faites à la main. Manches courtes, col rond.',
    'Ce top en coton biologique GOTS certifié est sublimé par des broderies florales réalisées à la main par nos artisanes. Chaque pièce est unique. Col rond légèrement décolleté, manches courtes avec broderie sur les ourlets. Légèrement transparent, prévoir une sous-couche.',
    '["https://images.unsplash.com/photo-1564257631407-4deb1f99d992?w=600&q=80","https://images.unsplash.com/photo-1551163943-3f7253a87c0d?w=600&q=80"]',
    '["XS","S","M","L"]',
    '[{"name":"Blanc cassé","hex":"#fefce8"},{"name":"Rose poudré","hex":"#fce7f3"}]',
    '{"XS":5,"S":10,"M":7,"L":3}',
    4.6, 18, '["coton bio","broderie","fait main"]', true, false, 'Promo'
) ON CONFLICT (id) DO NOTHING;

-- 3 - Pantalon Wide Leg Coton
INSERT INTO products (id, name, slug, category, price, original_price, description, long_description, images, sizes, colors, stock, rating, review_count, tags, featured, is_new, badge)
VALUES (
    3, 'Pantalon Wide Leg Coton', 'pantalon-wide-leg-coton', 'pantalons', 74.00, NULL,
    'Pantalon ample et confortable en coton doux. Coupe wide leg tendance, élastique à la taille.',
    'Le pantalon indispensable du dressing capsule. En coton léger de qualité supérieure, il offre un confort absolu tout au long de la journée. La coupe wide leg allonge la silhouette. Taille élastiquée avec cordon de serrage, deux poches latérales profondes.',
    '["https://images.unsplash.com/photo-1506629082955-511b1aa562c8?w=600&q=80","https://images.unsplash.com/photo-1551163943-3f7253a87c0d?w=600&q=80"]',
    '["XS","S","M","L","XL","XXL"]',
    '[{"name":"Écru","hex":"#f5f0e8"},{"name":"Noir","hex":"#1a1a1a"},{"name":"Kaki","hex":"#7d8c5c"}]',
    '{"XS":4,"S":9,"M":15,"L":11,"XL":5,"XXL":2}',
    4.7, 31, '["coton","confort","tendance"]', true, false, NULL
) ON CONFLICT (id) DO NOTHING;

-- 4 - Coussin Lin Rayé
INSERT INTO products (id, name, slug, category, price, original_price, description, long_description, images, sizes, colors, stock, rating, review_count, tags, featured, is_new, badge)
VALUES (
    4, 'Coussin Lin Rayé', 'coussin-lin-raye', 'maison', 38.00, NULL,
    'Coussin décoratif en lin naturel rayé. 45x45cm, housse amovible et lavable.',
    'Apportez une touche naturelle et chaleureuse à votre intérieur avec ce coussin en lin rayé. Tissu épais et résistant, texture légèrement texturée agréable au toucher. Fermeture invisible. Garnissage non inclus. Lavage en machine à 40°. Dimensions : 45x45cm.',
    '["https://images.unsplash.com/photo-1555041469-a586c61ea9bc?w=600&q=80","https://images.unsplash.com/photo-1506439773649-6e0eb8cfb237?w=600&q=80"]',
    '["45x45cm"]',
    '[{"name":"Naturel/Écru","hex":"#e8dcc8"},{"name":"Bleu/Naturel","hex":"#9bb5c7"},{"name":"Terracotta/Naturel","hex":"#c87b5c"}]',
    '{"45x45cm":20}',
    4.9, 42, '["lin","décoration","maison"]', true, false, 'Bestseller'
) ON CONFLICT (id) DO NOTHING;

-- 5 - Tablier Chef Coton
INSERT INTO products (id, name, slug, category, price, original_price, description, long_description, images, sizes, colors, stock, rating, review_count, tags, featured, is_new, badge)
VALUES (
    5, 'Tablier Chef Coton', 'tablier-chef-coton', 'cuisine', 32.00, NULL,
    'Tablier de cuisine en coton épais. Poche avant, bretelles réglables. Style bistrot.',
    'Ce tablier de cuisine style bistrot parisien est taillé dans un coton épais et résistant aux taches. Grande poche centrale pour vos ustensiles. Bretelles et liens croisés dans le dos entièrement réglables. Parfait pour cuisiner sans salir vos vêtements. Idée cadeau idéale.',
    '["https://images.unsplash.com/photo-1556909114-f6e7ad7d3136?w=600&q=80","https://images.unsplash.com/photo-1585540083814-ea6ee8af9e4f?w=600&q=80"]',
    '["Unique"]',
    '[{"name":"Écru","hex":"#f5f0e8"},{"name":"Noir","hex":"#1a1a1a"},{"name":"Marine","hex":"#1e3a5f"},{"name":"Rouge","hex":"#c0392b"}]',
    '{"Unique":25}',
    4.8, 56, '["coton","cuisine","cadeau"]', false, false, NULL
) ON CONFLICT (id) DO NOTHING;

-- 6 - Trousse de Toilette Lin
INSERT INTO products (id, name, slug, category, price, original_price, description, long_description, images, sizes, colors, stock, rating, review_count, tags, featured, is_new, badge)
VALUES (
    6, 'Trousse de Toilette Lin', 'trousse-toilette-lin', 'accessoires', 28.00, NULL,
    'Trousse de toilette en lin naturel avec fermeture éclair. Doublure imperméable. Taille voyage.',
    'La trousse de toilette idéale pour vos voyages. Lin naturel résistant à l''extérieur, doublure imperméable à l''intérieur facile à nettoyer. Fermeture éclair robuste. Un compartiment principal spacieux avec poche intérieure zippée. Dimensions : 22x15x8cm.',
    '["https://images.unsplash.com/photo-1607082348824-0a96f2a4b9da?w=600&q=80","https://images.unsplash.com/photo-1556228720-195a672e8a03?w=600&q=80"]',
    '["22x15cm"]',
    '[{"name":"Naturel","hex":"#e8dcc8"},{"name":"Marine","hex":"#1e3a5f"},{"name":"Kaki","hex":"#7d8c5c"}]',
    '{"22x15cm":18}',
    4.7, 29, '["lin","voyage","accessoire"]', false, true, 'Nouveau'
) ON CONFLICT (id) DO NOTHING;

-- 7 - Robe Midi Fleurie
INSERT INTO products (id, name, slug, category, price, original_price, description, long_description, images, sizes, colors, stock, rating, review_count, tags, featured, is_new, badge)
VALUES (
    7, 'Robe Midi Fleurie', 'robe-midi-fleurie', 'robes', 95.00, 120.00,
    'Robe midi à imprimé floral délicat. Tissu viscose fluide. Col V, manches longues avec élastique.',
    'Cette robe midi à imprimé floral est confectionnée dans une viscose légère et fluide qui tombe magnifiquement. Col V légèrement décolleté, manches longues avec élastique au poignet. Longueur midi qui arrive mi-mollet. Parfaite pour toutes les occasions.',
    '["https://images.unsplash.com/photo-1572804013309-59a88b7e92f1?w=600&q=80","https://images.unsplash.com/photo-1590086782957-93c06ef21604?w=600&q=80"]',
    '["XS","S","M","L","XL"]',
    '[{"name":"Fleuri fond écru","hex":"#fdf6ee"},{"name":"Fleuri fond bleu","hex":"#c8d8e8"}]',
    '{"XS":2,"S":6,"M":8,"L":4,"XL":1}',
    4.9, 38, '["viscose","fleuri","midi"]', true, false, 'Promo'
) ON CONFLICT (id) DO NOTHING;

-- 8 - Veste en Lin Oversize
INSERT INTO products (id, name, slug, category, price, original_price, description, long_description, images, sizes, colors, stock, rating, review_count, tags, featured, is_new, badge)
VALUES (
    8, 'Veste en Lin Oversize', 'veste-lin-oversize', 'vestes', 118.00, NULL,
    'Veste décontractée en lin froissé. Coupe oversize tendance, poches plaquées.',
    'La veste en lin est la pièce phare de la garde-robe minimaliste. En lin légèrement froissé au look décontracté assumé, cette veste oversize se porte sur tout. Deux poches plaquées sur le devant, boutons nacrés. Non doublée pour plus de légèreté. Teinture végétale.',
    '["https://images.unsplash.com/photo-1591047139829-d91aecb6caea?w=600&q=80","https://images.unsplash.com/photo-1594938298603-c8148c4b4459?w=600&q=80"]',
    '["S","M","L","XL"]',
    '[{"name":"Sable","hex":"#d4bc94"},{"name":"Blanc","hex":"#fefefe"},{"name":"Charbon","hex":"#3d3d3d"}]',
    '{"S":4,"M":7,"L":5,"XL":2}',
    4.6, 15, '["lin","oversize","veste"]', false, true, 'Nouveau'
) ON CONFLICT (id) DO NOTHING;

-- 9 - Tote Bag Lin Naturel
INSERT INTO products (id, name, slug, category, price, original_price, description, long_description, images, sizes, colors, stock, rating, review_count, tags, featured, is_new, badge)
VALUES (
    9, 'Tote Bag Lin Naturel', 'tote-bag-lin-naturel', 'accessoires', 22.00, NULL,
    'Grand tote bag en lin naturel épais. Anses longues. Format A4+. Zéro plastique.',
    'Ce tote bag spacieux en lin naturel épais est votre allié pour le quotidien écolo. Assez grand pour y glisser vos courses, vos livres ou votre laptop. Anses longues pour porter à l''épaule. Coutures renforcées. Fond cousu solide. Un choix durable et élégant.',
    '["https://images.unsplash.com/photo-1553062407-98eeb64c6a62?w=600&q=80","https://images.unsplash.com/photo-1547949003-9792a18a2601?w=600&q=80"]',
    '["Unique"]',
    '[{"name":"Naturel","hex":"#e8dcc8"},{"name":"Naturel/Marine","hex":"#9bb5c7"}]',
    '{"Unique":35}',
    4.8, 67, '["lin","éco","quotidien"]', false, false, NULL
) ON CONFLICT (id) DO NOTHING;

-- 10 - Chemise Lin Mandarin
INSERT INTO products (id, name, slug, category, price, original_price, description, long_description, images, sizes, colors, stock, rating, review_count, tags, featured, is_new, badge)
VALUES (
    10, 'Chemise Lin Mandarin', 'chemise-lin-mandarin', 'tops', 65.00, NULL,
    'Chemise en lin léger, col mao. Boutonnage devant, manches retroussables.',
    'Cette chemise en lin léger et respirant est parfaite pour la belle saison. Col mao élégant, boutonnage nacré sur le devant. Manches longues retroussables et fixables avec une petite patte boutonnée. Coupe légèrement cintrée. Se porte rentrée ou sur un pantalon.',
    '["https://images.unsplash.com/photo-1598300042247-d088f8ab3a91?w=600&q=80","https://images.unsplash.com/photo-1604644401890-0bd678c83788?w=600&q=80"]',
    '["XS","S","M","L","XL"]',
    '[{"name":"Blanc","hex":"#fefefe"},{"name":"Écru","hex":"#f5f0e8"},{"name":"Bleu ciel","hex":"#a8c5da"}]',
    '{"XS":3,"S":8,"M":10,"L":7,"XL":3}',
    4.7, 22, '["lin","chemise","été"]', false, false, NULL
) ON CONFLICT (id) DO NOTHING;

-- 11 - Jupe Longue Bohème
INSERT INTO products (id, name, slug, category, price, original_price, description, long_description, images, sizes, colors, stock, rating, review_count, tags, featured, is_new, badge)
VALUES (
    11, 'Jupe Longue Bohème', 'jupe-longue-boheme', 'jupes', 58.00, 72.00,
    'Jupe longue en coton imprimé ethnique. Taille élastiquée, volants en bas.',
    'Cette jupe longue à imprimé ethnique est une invitation au voyage. En coton léger imprimé, elle se porte autant en vacances qu''au quotidien. Taille entièrement élastiquée pour un confort maximal. Double volant en bas pour un effet bohème chic. Doublée jusqu''aux genoux.',
    '["https://images.unsplash.com/photo-1583496661160-fb5886a0aaaa?w=600&q=80","https://images.unsplash.com/photo-1585487000160-6ebcfceb0d03?w=600&q=80"]',
    '["S","M","L","XL"]',
    '[{"name":"Imprimé terracotta","hex":"#c87b5c"},{"name":"Imprimé bleu indigo","hex":"#3d5a80"}]',
    '{"S":5,"M":9,"L":6,"XL":2}',
    4.5, 19, '["coton","bohème","jupe longue"]', false, false, 'Promo'
) ON CONFLICT (id) DO NOTHING;

-- 12 - Nappe Lin Naturel
INSERT INTO products (id, name, slug, category, price, original_price, description, long_description, images, sizes, colors, stock, rating, review_count, tags, featured, is_new, badge)
VALUES (
    12, 'Nappe Lin Naturel', 'nappe-lin-naturel', 'maison', 48.00, NULL,
    'Nappe en lin lavé 140x250cm. Finitions soignées, ourlets faits main. Anti-taches.',
    'Habiller votre table avec cette nappe en lin lavé au rendu mat et au toucher doux. Le lin lavé est pré-rétréci pour un entretien simple. Ourlets soignés faits à la machine. Légèrement anti-taches grâce à un traitement naturel. Dimensions : 140x250cm. Lavage 40°.',
    '["https://images.unsplash.com/photo-1615529162924-f8605388461d?w=600&q=80","https://images.unsplash.com/photo-1556909114-f6e7ad7d3136?w=600&q=80"]',
    '["140x250cm"]',
    '[{"name":"Naturel","hex":"#e8dcc8"},{"name":"Blanc","hex":"#fefefe"},{"name":"Gris clair","hex":"#d0cdc8"}]',
    '{"140x250cm":14}',
    4.9, 33, '["lin","table","maison"]', false, false, NULL
) ON CONFLICT (id) DO NOTHING;

-- ============================================================
-- Reviews
-- ============================================================
INSERT INTO reviews (id, product_id, user_id, user_name, rating, comment, date, verified)
VALUES (1, 1, 'user1', 'Marie L.', 5,
    'Magnifique robe, le lin est d''une qualité exceptionnelle. Je l''ai prise en terracotta et la couleur est vraiment belle. Je recommande !',
    '2024-03-15', true) ON CONFLICT (id) DO NOTHING;

INSERT INTO reviews (id, product_id, user_id, user_name, rating, comment, date, verified)
VALUES (2, 1, 'user2', 'Sophie R.', 5,
    'Conforme à la description. La taille est fidèle. Belle finition.',
    '2024-02-20', true) ON CONFLICT (id) DO NOTHING;

INSERT INTO reviews (id, product_id, user_id, user_name, rating, comment, date, verified)
VALUES (3, 4, 'user3', 'Emma B.', 5,
    'Coussin superbe, la qualité du lin est remarquable. Très bon retour positif de mes invités !',
    '2024-03-01', true) ON CONFLICT (id) DO NOTHING;
